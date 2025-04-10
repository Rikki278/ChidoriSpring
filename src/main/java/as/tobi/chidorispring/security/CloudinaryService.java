package as.tobi.chidorispring.security;

import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        Dotenv dotenv = Dotenv.load();
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", dotenv.get("CLOUDINARY_CLOUD_NAME"),
                "api_key", dotenv.get("CLOUDINARY_API_KEY"),
                "api_secret", dotenv.get("CLOUDINARY_API_SECRET")
        ));
    }

    public String uploadProfilePicture(MultipartFile file) {
        try {
            var result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new InternalViolationException(InternalViolationType.FILE_PROCESSING_ERROR);
        }
    }

    // Image removal method
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // Извлекаем public_id из URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (IOException e) {
            throw new InternalViolationException(InternalViolationType.FILE_DELETION_ERROR);
        }
    }


    private String extractPublicIdFromUrl(String imageUrl) {

        try {
            String[] parts = imageUrl.split("/upload/")[1].split("/");
            String lastPart = parts[parts.length - 1];
            // We delete the version (v1234567) if there is
            return lastPart.contains("v") ? lastPart.split("v")[1].substring(1) : lastPart.split("\\.")[0];
        } catch (Exception e) {
            throw new InternalViolationException(InternalViolationType.INVALID_IMAGE_URL);
        }
    }


}

