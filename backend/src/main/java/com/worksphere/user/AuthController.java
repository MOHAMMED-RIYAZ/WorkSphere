
package com.worksphere.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
private final LoginService loginService;
    public AuthController(AuthService authService, LoginService loginService) {
    this.authService = authService;
    this.loginService = loginService;
}
    
@PostMapping("/login")
public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return loginService.login(request);
}


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request) {

        AppUser user = authService.register(
                request.name(),
                request.email(),
                request.password()
        );

        return AuthResponse.from(user);
    }
}
