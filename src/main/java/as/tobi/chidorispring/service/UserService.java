package as.tobi.chidorispring.service;

import as.tobi.chidorispring.dto.auth.RegisterRequest;
import as.tobi.chidorispring.dto.userProfile.UserProfileDTO;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import as.tobi.chidorispring.mapper.UserMapper;
import as.tobi.chidorispring.repository.UserRepository;
import as.tobi.chidorispring.config.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CloudinaryService cloudinaryService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;  // 5 MB = 5242880 byte

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


    public void updateUserAvatar(String email, MultipartFile avatar) {
        UserProfile user = getUserByEmail(email);

        if (avatar.getSize() > MAX_FILE_SIZE) {  // 5 MB
            throw new InternalViolationException(InternalViolationType.FILE_TOO_LARGE);
        }

        String imageUrl = cloudinaryService.uploadAvatar(avatar);
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
    }

    public UserProfileDTO getUserProfile(String email) {
        UserProfile user = getUserByEmail(email);
        return userMapper.toUserProfileDto(user);
    }

    private UserProfile getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS));
    }
}

