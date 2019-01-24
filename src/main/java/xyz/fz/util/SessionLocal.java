package xyz.fz.util;

import xyz.fz.entity.User;

public class SessionLocal {

    private static ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    public static User getUser() {
        return (User) threadLocal.get();
    }

    public static void setUser(User user) {
        threadLocal.set(user);
    }

    public static void remove() {
        threadLocal.remove();
    }
}
