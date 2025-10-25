package fpt.swp.springmvctt.itp.util;

import jakarta.servlet.http.HttpSession;

public class ValidateUtil {

    public boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return role != null && role.equalsIgnoreCase("ADMIN");
    }

}
