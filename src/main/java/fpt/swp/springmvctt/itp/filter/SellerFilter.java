package fpt.swp.springmvctt.itp.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = "/shop/*")
public class SellerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false); // lấy session nếu có

        // lấy role của user trong session
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        if (role == null || !role.equalsIgnoreCase("SELLER")) {
            // Nếu không có quyền
            res.sendRedirect(req.getContextPath() + "/?message=not_authorized");
            return; // không cho đi tiếp
        }


        // Nếu có quyền → đi tiếp
        chain.doFilter(request, response);
    }
}
