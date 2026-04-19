package com.smartinvoice.service;

import com.smartinvoice.dto.AuthResponse;
import com.smartinvoice.dto.LoginRequest;
import com.smartinvoice.dto.RegisterRequest;
import com.smartinvoice.entity.User;
import com.smartinvoice.exception.DuplicateResourceException;
import com.smartinvoice.repository.UserRepository;
import com.smartinvoice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setRole("STAFF");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(User.Role.STAFF);
    }

    // ─────────────────────────────────────────────
    // TEST 1: Register — Success
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should register user successfully")
    void testRegister_Success() {

        // ARRANGE
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString(), anyString()))
                .thenReturn("mockJwtToken");

        // ACT
        AuthResponse response =
                authService.register(registerRequest);

        // ASSERT
        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("STAFF", response.getRole());
        assertEquals("User registered successfully",
                response.getMessage());

        verify(userRepository, times(1))
                .findByUsername("testuser");
        verify(passwordEncoder, times(1))
                .encode("password123");
        verify(userRepository, times(1))
                .save(any(User.class));
    }

    // ─────────────────────────────────────────────
    // TEST 2: Register — Duplicate Username
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should throw exception for duplicate username")
    void testRegister_DuplicateUsername() {

        // ARRANGE: username already exists
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(savedUser));

        // ACT + ASSERT
        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(registerRequest));

        assertTrue(ex.getMessage()
                .contains("already taken"));

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ─────────────────────────────────────────────
    // TEST 3: Register Admin Role
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should register user with ADMIN role")
    void testRegister_AdminRole() {

        // ARRANGE
        registerRequest.setRole("ADMIN");

        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("testuser");
        adminUser.setPassword("encodedPassword");
        adminUser.setRole(User.Role.ADMIN);

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(adminUser);
        when(jwtUtil.generateToken(anyString(), anyString()))
                .thenReturn("adminToken");

        // ACT
        AuthResponse response =
                authService.register(registerRequest);

        // ASSERT
        assertEquals("ADMIN", response.getRole());
        assertEquals("adminToken", response.getToken());
    }

    // ─────────────────────────────────────────────
    // TEST 4: Login — Success
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should login successfully")
    void testLogin_Success() {

        // ARRANGE
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // auth passes

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(savedUser));
        when(jwtUtil.generateToken("testuser", "STAFF"))
                .thenReturn("loginToken");

        // ACT
        AuthResponse response =
                authService.login(loginRequest);

        // ASSERT
        assertNotNull(response);
        assertEquals("loginToken", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("STAFF", response.getRole());
        assertEquals("Login successful",
                response.getMessage());
    }

    // ─────────────────────────────────────────────
    // TEST 5: Login — Bad Credentials
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should throw exception for bad credentials")
    void testLogin_BadCredentials() {

        // ARRANGE: auth manager throws exception
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(
                        "Bad credentials"));

        // ACT + ASSERT
        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest));

        // Should never reach findByUsername
        verify(userRepository, never())
                .findByUsername(anyString());
    }
}