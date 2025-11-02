package fpt.swp.springmvctt.itp.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

@Controller
@PropertySource("classpath:google.properties")
public class GoogleOAuthController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthController.class);

    @Autowired
    private UserService userService;

    @Value("${google.clientId}")
    private String clientId;

    @Value("${google.clientSecret}")
    private String clientSecret;

    @Value("${google.redirectUri:http://localhost:8080/itp/oauth2/callback/google}")
    private String redirectUri;

    public GoogleOAuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/oauth2/authorize/google")
    public String authorizeWithGoogle() {
        try {
            // Kiểm tra cấu hình Google OAuth
            if (clientId == null || clientId.trim().isEmpty()) {
                logger.error("Google Client ID is not configured");
                return "redirect:/login?error=oauth_not_configured";
            }

            String scope = URLEncoder.encode("openid email profile", "UTF-8");
            String state = URLEncoder.encode(String.valueOf(System.currentTimeMillis()), "UTF-8");

            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                    + "?client_id=" + URLEncoder.encode(clientId, "UTF-8")
                    + "&response_type=code"
                    + "&scope=" + scope
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                    + "&state=" + state
                    + "&access_type=offline"
                    + "&prompt=select_account";

            logger.info("Redirecting to Google OAuth: {}", authUrl);
            return "redirect:" + authUrl;
        } catch (UnsupportedEncodingException e) {
            logger.error("Error encoding OAuth parameters", e);
            return "redirect:/login?error=oauth_error";
        }
    }

    @GetMapping("/oauth2/callback/google")
    public String callback(@RequestParam(name = "code", required = false) String code,
                           @RequestParam(name = "error", required = false) String error,
                           @RequestParam(name = "state", required = false) String state,
                           HttpSession session,
                           RedirectAttributes ra) {

        logger.info("Google OAuth callback received - code: {}, error: {}",
                code != null ? "present" : "null", error);

        if (error != null) {
            logger.error("Google OAuth error: {}", error);
            ra.addFlashAttribute("error", "Đăng nhập Google thất bại: " + error);
            return "redirect:/login";
        }

        if (code == null || code.trim().isEmpty()) {
            logger.error("No authorization code received from Google");
            ra.addFlashAttribute("error", "Không nhận được mã xác thực từ Google.");
            return "redirect:/login";
        }

        try {
            // Trao đổi authorization code lấy access token
            String params = "code=" + URLEncoder.encode(code, "UTF-8")
                    + "&client_id=" + URLEncoder.encode(clientId, "UTF-8")
                    + "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8")
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                    + "&grant_type=authorization_code";

            logger.debug("Requesting access token from Google");
            URL url = new URL("https://oauth2.googleapis.com/token");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Accept", "application/json");

            try (OutputStream os = con.getOutputStream()) {
                os.write(params.getBytes("UTF-8"));
            }

            int status = con.getResponseCode();
            logger.debug("Google token response status: {}", status);

            InputStream is = (status >= 200 && status < 400) ? con.getInputStream() : con.getErrorStream();
            ObjectMapper mapper = new ObjectMapper();
            Map<?,?> tokenResp = mapper.readValue(is, Map.class);

            if (status >= 400) {
                logger.error("Google token error response: {}", tokenResp);
                ra.addFlashAttribute("error", "Lỗi xác thực với Google. Vui lòng thử lại.");
                return "redirect:/login";
            }

            String accessToken = (String) tokenResp.get("access_token");
            if (accessToken == null || accessToken.trim().isEmpty()) {
                logger.error("No access token in Google response: {}", tokenResp);
                ra.addFlashAttribute("error", "Không thể lấy access token từ Google.");
                return "redirect:/login";
            }

            // Lấy thông tin user từ Google
            logger.debug("Fetching user info from Google");
            URL infoUrl = new URL("https://www.googleapis.com/oauth2/v2/userinfo");
            HttpURLConnection infoCon = (HttpURLConnection) infoUrl.openConnection();
            infoCon.setRequestMethod("GET");
            infoCon.setRequestProperty("Authorization", "Bearer " + accessToken);
            infoCon.setRequestProperty("Accept", "application/json");

            int infoStatus = infoCon.getResponseCode();
            logger.debug("Google userinfo response status: {}", infoStatus);

            InputStream is2 = (infoStatus >= 200 && infoStatus < 400) ?
                    infoCon.getInputStream() : infoCon.getErrorStream();
            Map<?,?> userinfo = mapper.readValue(is2, Map.class);

            if (infoStatus >= 400) {
                logger.error("Google userinfo error response: {}", userinfo);
                ra.addFlashAttribute("error", "Không thể lấy thông tin người dùng từ Google.");
                return "redirect:/login";
            }

            String email = (String) userinfo.get("email");
            String name = (String) userinfo.get("name");
            String picture = (String) userinfo.get("picture");

            logger.info("Google OAuth success for email: {}", email);

            if (email == null || email.trim().isEmpty()) {
                logger.error("No email in Google userinfo response: {}", userinfo);
                ra.addFlashAttribute("error", "Không thể lấy email từ tài khoản Google.");
                return "redirect:/login";
            }

            // Tạo hoặc tìm user từ thông tin OAuth
            User u = userService.findOrCreateFromOAuth(email, name);
            if (u == null) {
                logger.error("Could not create/find user for email: {}", email);
                ra.addFlashAttribute("error", "Không thể tạo tài khoản từ Google. Vui lòng thử lại.");
                return "redirect:/login";
            }

            logger.info("User logged in successfully via Google: {}", email);
            session.setAttribute("user", u);
            session.setAttribute("role", u.getRole() != null ? u.getRole().getName() : "CUSTOMER");
            ra.addFlashAttribute("success", "Đăng nhập Google thành công! Chào mừng " + u.getFullName());
            return "redirect:/";

        } catch (Exception ex) {
            logger.error("Exception during Google OAuth", ex);
            ra.addFlashAttribute("error", "Lỗi trong quá trình đăng nhập Google: " + ex.getMessage());
            return "redirect:/login";
        }
    }
}