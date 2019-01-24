package xyz.fz.service.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.fz.dao.CommonDao;
import xyz.fz.entity.User;
import xyz.fz.service.UserService;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private CommonDao db;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        db.save(user);
        return user;
    }

    @Override
    public User load(Long userId) {
        return db.findById(User.class, userId);
    }

    @Override
    public User load(String username) {
        HashMap<String, Object> params = new HashMap<>();
        String sql = "select * from t_user where username = :username ";
        params.put("username", username);
        List<User> list = db.queryListBySql(sql, params, User.class);
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void delete(Long userId) {
        db.delete(load(userId));
    }

    @Override
    public boolean passwordMatches(User user, String password) {
        return bCryptPasswordEncoder.matches(password, user.getPassword());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void versionUpdate(User user) {
        user.setVersion(user.getVersion() + 1);
        db.update(user);
    }
}
