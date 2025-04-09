package as.tobi.chidorispring.mapper;

import as.tobi.chidorispring.dto.auth.RegisterRequest;
import as.tobi.chidorispring.dto.userProfile.UserProfileDTO;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    public UserProfile toUserEntity(RegisterRequest request) {
        return UserProfile.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    public UserProfileDTO toUserProfileDto(UserProfile user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImageUrl(user.getProfileImageUrl())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // If you need to turn on the avatar as Base64
    public UserProfileDTO toUserProfileDtoWithAvatar(UserProfile user) {
        UserProfileDTO dto = toUserProfileDto(user);
        if (user.getProfileImage() != null) {
            dto.setAvatarBase64(java.util.Base64.getEncoder().encodeToString(user.getProfileImage()));
        }
        return dto;
    }
}