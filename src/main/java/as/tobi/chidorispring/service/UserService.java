package as.tobi.chidorispring.service;

import as.tobi.chidorispring.dto.auth.RegisterRequest;
import as.tobi.chidorispring.dto.userProfile.UserProfileDTO;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.enums.UserRole;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import as.tobi.chidorispring.mapper.UserMapper;
import as.tobi.chidorispring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public UserProfile saveUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InternalViolationException(InternalViolationType.USER_ALREADY_EXISTS);
        }

        UserProfile user = userMapper.toUserEntity(request);
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserProfile user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS));
        return new User(user.getEmail(), user.getPassword(), List.of());
    }

    public void updateUserAvatar(String email, MultipartFile avatar) throws IOException {
        UserProfile user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS));

        user.setProfileImage(avatar.getBytes());
        user.setProfileImageUrl(generateImageUrl(avatar.getOriginalFilename()));
        userRepository.save(user);
    }

    public byte[] getUserAvatar(String email) {
        UserProfile user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS));

        return user.getProfileImage();
    }

    private String generateImageUrl(String filename) {
        // Generation of a unique file name or URL
        return UUID.randomUUID() + "_" + filename;
    }

    public UserProfileDTO getUserProfile(String email) {
        UserProfile user = getUserByEmail(email);
        return userMapper.toUserProfileDto(user);
    }

    public UserProfileDTO getUserProfileWithAvatar(String email) {
        UserProfile user = getUserByEmail(email);
        return userMapper.toUserProfileDtoWithAvatar(user);
    }

    private UserProfile getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS));
    }
}

