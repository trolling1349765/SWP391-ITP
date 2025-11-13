package fpt.swp.springmvctt.itp.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SellerFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(SellerFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        String requestPath = req.getRequestURI();
        String contextPath = req.getContextPath();
        
        // Normalize paths để tránh vấn đề với trailing slashes
        String normalizedPath = requestPath.replace(contextPath, "");
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        
        logger.debug("SellerFilter: Checking path: {}", normalizedPath);
        
        // Exclude shop registration URLs - cho phép CUSTOMER đăng ký shop
        // Cho phép cả /shop/register và /shop/register/cancel
        if (normalizedPath.equals("/shop/register") || 
            normalizedPath.startsWith("/shop/register/")) {
            // Cho phép CUSTOMER truy cập trang đăng ký shop
            logger.debug("SellerFilter: Allowing shop registration path: {}", normalizedPath);
            chain.doFilter(request, response);
            return;
        }
        
        HttpSession session = req.getSession(false); // lấy session nếu có

        // lấy role của user trong session
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        
        logger.debug("SellerFilter: Path: {}, Role: {}", normalizedPath, role);

        if (role == null || !role.equalsIgnoreCase("SELLER")) {
            // Nếu không có quyền
            logger.warn("SellerFilter: Access denied for path: {}, role: {}", normalizedPath, role);
            res.sendRedirect(contextPath + "/?message=not_authorized");
            return; // không cho đi tiếp
        }

        // Nếu có quyền → đi tiếp
        logger.debug("SellerFilter: Access granted for path: {}, role: {}", normalizedPath, role);
        chain.doFilter(request, response);
    }
}
