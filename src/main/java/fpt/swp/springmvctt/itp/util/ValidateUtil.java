package fpt.swp.springmvctt.itp.util;

import java.util.regex.Pattern;

public class ValidateUtil {

    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$";

    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return emailPattern.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return passwordPattern.matcher(password).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // Vietnamese phone number pattern
        String phonePattern = "^(\\+84|0)[0-9]{9,10}$";
        return Pattern.matches(phonePattern, phone.replaceAll("\\s+", ""));
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        // Username: 3-20 characters, alphanumeric and underscore only
        String usernamePattern = "^[a-zA-Z0-9_]{3,20}$";
        return Pattern.matches(usernamePattern, username);
    }
}
