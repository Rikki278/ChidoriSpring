package as.tobi.chidorispring.controller;

import as.tobi.chidorispring.dto.auth.AuthResponse;
import as.tobi.chidorispring.dto.auth.LoginRequest;
import as.tobi.chidorispring.dto.auth.RegisterRequest;
import as.tobi.chidorispring.dto.auth.RegisterResponse;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.service.UserService;
import as.tobi.chidorispring.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = jwtUtil.generateToken(request.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(request.getEmail());

        AuthResponse response = new AuthResponse(accessToken, refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserProfile user = userService.saveUser(request);
        return ResponseEntity.ok(new RegisterResponse(user.getId(), "Registration successful"));
    }

}
