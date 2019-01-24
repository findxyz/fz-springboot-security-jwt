package xyz.fz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.fz.entity.User;
import xyz.fz.model.Result;
import xyz.fz.service.UserService;
import xyz.fz.util.JwtUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Controller
public class LoginController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return "login/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          HttpServletResponse httpServletResponse) {
        User user = userService.load(username);
        if (user == null) {
            return Result.ofMessage("用户名或密码错误");
        }
        if (!userService.passwordMatches(user, password)) {
            return Result.ofMessage("用户名或密码错误");
        }
        String jwt = jwtUtil.createJwt(user.getId() + "", username, new Date(), user.getVersion());
        httpServletResponse.addCookie(jwtUtil.jwtCookie(jwt));
        return Result.ofSuccess();
    }

    @RequestMapping("/logout")
    @ResponseBody
    public Result logout(HttpServletResponse httpServletResponse) {
        httpServletResponse.addCookie(jwtUtil.clearJwtCookie());
        return Result.ofSuccess();
    }
}
