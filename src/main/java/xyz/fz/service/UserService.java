package xyz.fz.service;

import xyz.fz.entity.User;

public interface UserService {
    User save(User user);

    User load(Long userId);

    User load(String username);

    void delete(Long userId);

    boolean passwordMatches(User user, String password);

    void versionUpdate(User user);
}
