package xyz.fz.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.fz.entity.User;
import xyz.fz.model.Result;
import xyz.fz.service.UserService;
import xyz.fz.util.SessionLocal;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @RequestMapping("/versionUpdate")
    public Result versionUpdate() {
        User user = SessionLocal.getUser();
        userService.versionUpdate(user);
        return Result.ofSuccess();
    }
}
