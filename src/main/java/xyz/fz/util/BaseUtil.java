package xyz.fz.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by fz on 2016/9/7.
 */
public class BaseUtil {

    private static final Logger logger = LoggerFactory.getLogger(BaseUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T parseJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }

    public static String jaxbMarshal(Object obj) throws IOException, JAXBException {
        String xml = "";
        try (StringWriter sw = new StringWriter()) {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty("com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler",
                    new CharacterEscapeHandler() {
                        @Override
                        public void escape(char[] ch, int start, int length, boolean isAttVal, Writer writer) throws IOException {
                            writer.write(ch, start, length);
                        }
                    });
            sw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            marshaller.marshal(obj, sw);
            xml = sw.toString();
        }
        return xml;
    }

    @SuppressWarnings("unchecked")
    public static <T> T jaxbUnMarshal(String xml, Class<T> cls) throws JAXBException {
        try (StringReader sr = new StringReader(xml)) {
            JAXBContext context = JAXBContext.newInstance(cls);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(sr);
        }
    }

    private static DocumentBuilder newDocumentBuilder() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            return documentBuilderFactory.newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Document newDocument() {
        return newDocumentBuilder().newDocument();
    }

    public static Map<String, String> xmlToMap(String strXML) {
        try {
            Map<String, String> data = new HashMap<>();
            InputStream stream = new ByteArrayInputStream(strXML.getBytes(StandardCharsets.UTF_8));
            Document doc = newDocumentBuilder().parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            try {
                stream.close();
            } catch (Exception ex) {
                // do nothing
            }
            return data;
        } catch (Exception ex) {
            logger.error("Invalid XML, can not convert to map. Error message: {}. XML content: {}", ex.getMessage(), strXML);
            return null;
        }
    }

    public static String mapToXml(Map<String, String> data) {
        String output = "";
        try {
            Document document = newDocument();
            org.w3c.dom.Element root = document.createElement("xml");
            document.appendChild(root);
            for (String key : data.keySet()) {
                String value = data.get(key);
                if (value == null) {
                    value = "";
                }
                value = value.trim();
                org.w3c.dom.Element filed = document.createElement(key);
                filed.appendChild(document.createTextNode(value));
                root.appendChild(filed);
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            output = writer.getBuffer().toString();
            writer.close();
        } catch (Exception ignored) {
        }
        return output;
    }

    public static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        int maxValue = 255;
        if (fc > maxValue) {
            fc = maxValue;
        }
        if (bc > maxValue) {
            bc = maxValue;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static final ThreadLocal<DateFormat> SHORT_DF = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private static final ThreadLocal<DateFormat> SHORT_DESC_DF = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy年MM月dd日");
        }
    };

    private static final ThreadLocal<DateFormat> LONG_DF = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    private static final ThreadLocal<DateFormat> NUMBER_GENERATOR_DF = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyMMddHHmmssSSS");
        }
    };

    public static String toShortDate(Date date) {
        return SHORT_DF.get().format(date);
    }

    public static String toShortDescDate(Date date) {
        return SHORT_DESC_DF.get().format(date);
    }

    public static String toLongDate(Date date) {
        return LONG_DF.get().format(date);
    }

    public static Date toLongDate(String dateStr) {
        Date date = new Date();
        try {
            date = LONG_DF.get().parse(dateStr);
        } catch (ParseException ignore) {
        }
        return date;
    }

    public static String getExceptionStackTrace(Exception e) {

        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            return sw.toString();
        } finally {
            try {
                if (sw != null) {
                    sw.close();
                }
                if (pw != null) {
                    pw.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Map<String, String> data = new HashMap<>();
        data.put("aa", "bb");
        data.put("cc", "dd");
        data.put("ee", "ff");
        String xml = mapToXml(data);
        Map<String, String> map = xmlToMap(xml);
        System.out.println(xml);
        System.out.println(map);
    }
}
