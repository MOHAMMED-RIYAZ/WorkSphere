
package com.worksphere;
import com.worksphere.user.LoginService;
import com.worksphere.user.AppUser;
import com.worksphere.user.AppUserRepository;
import com.worksphere.user.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.worksphere.JwtService;
@WebMvcTest(com.worksphere.user.AuthController.class)
@Import(AuthService.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
private JwtService jwtService;

    @MockitoBean
    private AppUserRepository appUserRepository;
@MockitoBean
private LoginService loginService;
    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void registerValidUserReturnsCreated() throws Exception {
        when(appUserRepository.existsByEmailIgnoreCase(anyString()))
                .thenReturn(false);

        when(passwordEncoder.encode("TestPass123!"))
                .thenReturn("mock-bcrypt-hash");

       when(appUserRepository.save(any(AppUser.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Riyaz Test",
                                    "email": "riyaz.test@example.com",
                                    "password": "TestPass123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.name").value("Riyaz Test"))
                .andExpect(jsonPath("$.email")
                        .value("riyaz.test@example.com"));

        verify(passwordEncoder).encode("TestPass123!");
    }

    @Test
    void registerDuplicateEmailReturnsConflict() throws Exception {
        when(appUserRepository.existsByEmailIgnoreCase(anyString()))
                .thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Riyaz Test",
                                    "email": "riyaz.test@example.com",
                                    "password": "TestPass123!"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void registerInvalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Riyaz Test",
                                    "email": "not-an-email",
                                    "password": "TestPass123!"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerShortPasswordReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Riyaz Test",
                                    "email": "riyaz.test@example.com",
                                    "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
