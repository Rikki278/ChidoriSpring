package as.tobi.chidorispring.service;

import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
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
            log.debug("Image URL is null or empty, skipping deletion");
            return;
        }

        try {
            // Public_id from URL
            String publicId = extractPublicIdFromUrl(imageUrl);

            if (publicId != null) {
                log.debug("Deleting image from Cloudinary with public_id: {}", publicId);
                var result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deletion result: {}", result);
            } else {
                log.warn("Could not extract public_id from URL: {}", imageUrl);
            }
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary. URL: {}, Error: {}", imageUrl, e.getMessage());
            throw new InternalViolationException(InternalViolationType.FILE_DELETION_ERROR);
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // example URL: https://res.cloudinary.com/djmpkplp1/image/upload/v1744228994/folder/rrhwafrprajyoygexwyv.jpg
            String[] uploadParts = imageUrl.split("/upload/");
            if (uploadParts.length < 2) {
                log.error("Invalid Cloudinary URL format (missing /upload/): {}", imageUrl);
                throw new InternalViolationException(InternalViolationType.INVALID_IMAGE_URL);
            }

            // take part after/Upload/: V1744228994/Folder/RRHWAFRPRAJYOYGEXWYV.jpg
            String afterUpload = uploadParts[1];
            String[] parts = afterUpload.split("/");

            // if there is a version (V1744228994), it will be the first element
            int startIndex = parts[0].startsWith("v") ? 1 : 0;

            // collect public_id, starting from the Startindex index
            StringBuilder publicIdBuilder = new StringBuilder();
            for (int i = startIndex; i < parts.length; i++) {
                publicIdBuilder.append(parts[i]);
                if (i < parts.length - 1) {
                    publicIdBuilder.append("/");
                }
            }

            String publicIdWithExtension = publicIdBuilder.toString(); // folder/rrhwafrprajyoygexwyv.jpg
            String publicId = publicIdWithExtension.substring(0, publicIdWithExtension.lastIndexOf(".")); // folder/rrhwafrprajyoygexwyv
            log.debug("Extracted public_id: {} from URL: {}", publicId, imageUrl);
            return publicId;
        } catch (Exception e) {
            log.error("Failed to extract public_id from URL: {}. Error: {}", imageUrl, e.getMessage());
            throw new InternalViolationException(InternalViolationType.INVALID_IMAGE_URL);
        }
    }


}

