package fpt.swp.springmvctt.itp.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/admin/*", "/shop/registers"})
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false); // lấy session nếu có

        // lấy role của user trong session
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            // Nếu không có quyền
            res.sendRedirect(req.getContextPath() + "/home?message=not_authorized");
            return; // không cho đi tiếp
        }

        // Nếu có quyền → đi tiếp
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
