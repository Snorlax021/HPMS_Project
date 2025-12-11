package Service;

import Model.Role;
import java.util.Optional;
import java.util.Arrays;

public class UserServiceTest {
    public static void main(String[] args) {
        UserService svc = new UserService();
        svc.createDefaultDemoUsers();
        char[] pwd = "admin123".toCharArray();
        Optional<Model.User> u = svc.authenticate("admin", pwd);
        System.out.println("Auth admin: " + u.isPresent());
        // change password
        char[] oldPwd = "admin123".toCharArray();
        char[] newPwd = "admin456".toCharArray();
        char[] newPwdForAuth = Arrays.copyOf(newPwd, newPwd.length);
        boolean changed = svc.changePassword("admin", oldPwd, newPwd);
        System.out.println("Changed password: " + changed);
        Optional<Model.User> u2 = svc.authenticate("admin", newPwdForAuth);
        System.out.println("Auth with new password: " + u2.isPresent());
    }
}