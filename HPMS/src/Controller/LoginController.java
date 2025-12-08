package Controller;

import Service.LoginService;

public class LoginController {
    private LoginService loginService;

    public LoginController() { this.loginService = null; }

    public LoginController(LoginService service) {
        this.loginService = service;
    }

    public boolean authenticate(String username, String password) {
        if (loginService == null) throw new IllegalStateException("LoginService not initialized");
        return loginService.validateCredentials(username, password);
    }
}
