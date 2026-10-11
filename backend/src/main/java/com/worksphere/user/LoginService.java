
package com.worksphere.user;

import com.worksphere.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@Service
public class LoginService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository
                .findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new LoginException());

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new LoginException();
        }

        String token = jwtService.generateToken(user.getEmail());

        return LoginResponse.from(user, token);
    }
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class LoginException extends RuntimeException {
        public LoginException() {
            super("Invalid email or password");
        }
    }
}
