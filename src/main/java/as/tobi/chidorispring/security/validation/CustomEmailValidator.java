package as.tobi.chidorispring.security.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.InetAddress;

public class CustomEmailValidator implements ConstraintValidator<ValidEmail, String> {

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        if (!isBasicFormatValid(email)) {
            return false;
        }

        String domain = extractDomain(email);
        return domain != null && isDomainValid(domain);
    }

    // Check the basic email format
    private boolean isBasicFormatValid(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Extract the domain from the email
    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return null;
        }
        return email.substring(atIndex + 1);
    }

    // Check through DNS
    private boolean isDomainValid(String domain) {
        try {
            // Only checking if the domain can resolve, result is not needed
            InetAddress.getByName(domain);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}