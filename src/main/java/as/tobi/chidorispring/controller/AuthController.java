package as.tobi.chidorispring.controller;
import as.tobi.chidorispring.dto.LoginRequest;
import as.tobi.chidorispring.dto.RegisterRequest;
import as.tobi.chidorispring.service.UserService;
import as.tobi.chidorispring.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        return jwtUtil.generateToken(request.getEmail());
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        userService.saveUser(request.getEmail(), request.getPassword());
        return "User registered";
    }
}
