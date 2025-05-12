package as.tobi.chidorispring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import as.tobi.chidorispring.utils.JwtAuthenticationFilter;

import java.util.List;

@Configuration
class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOrigins(List.of("http://localhost:5173"));
                    corsConfig.setAllowedMethods(List.of("*"));
                    corsConfig.setAllowedHeaders(List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/anime/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/chat/**").permitAll()
                        .requestMatchers("/ws-chat/**").permitAll()
                        .requestMatchers("/api/recommendations/**").permitAll()

                        // Admin only endpoints
                        .requestMatchers("/api/users/*/delete").hasRole("ADMIN")
                        .requestMatchers("/api/posts/*/delete").hasRole("ADMIN")

                        // User specific endpoints (users can only access their own data)
                        .requestMatchers("/api/users/avatar").authenticated()
                        .requestMatchers("/api/users/all").authenticated()
                        .requestMatchers("/api/users/profile").authenticated()
                        .requestMatchers("/api/users/profile-posts").authenticated()
                        .requestMatchers("/api/users/{userId}").access((authentication, context) -> {
                            Authentication currentAuth = authentication.get();
                            String requestedUserId = context.getVariables().get("userId");
                            String currentUserEmail = currentAuth.getName();
                            // Allow if user is accessing their own data or is admin
                            boolean isAllowed = currentAuth.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                                    currentUserEmail.equals(requestedUserId);
                            return new AuthorizationDecision(isAllowed);
                        })

                        // Post related endpoints
                        .requestMatchers("/api/posts/**").authenticated()
                        .requestMatchers("/api/posts/*/comments/**").authenticated()
                        .requestMatchers("/api/posts/*/comments/{commentId}").access((authentication, context) -> {
                            Authentication currentAuth = authentication.get();
                            // Allow if user is admin or the comment owner
                            boolean isAllowed = currentAuth.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                                    // Add your comment ownership check logic here
                                    true; // Replace with actual comment ownership check
                            return new AuthorizationDecision(isAllowed);
                        })
                        
                        // Default to authenticated for all other endpoints
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}

