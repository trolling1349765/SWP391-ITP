package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.service.ShopContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopContextImpl implements ShopContext {

    private static final String KEY = "CURRENT_SHOP_ID";
    private final HttpServletRequest request;

    @Override
    public long currentShopId() {
        HttpSession session = request.getSession(true);

        Long fromSession = toLong(session.getAttribute(KEY));
        if (fromSession != null) return fromSession;

        Long fromParam = toLong(request.getParameter("shopId"));
        if (fromParam != null) {
            session.setAttribute(KEY, fromParam);
            return fromParam;
        }

        long fallback = 1L; // SHOP id mặc định để chạy khi chưa login
        session.setAttribute(KEY, fallback);
        return fallback;
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof String s) {
            try { return s.isBlank() ? null : Long.parseLong(s.trim()); }
            catch (NumberFormatException ignored) { }
        }
        return null;
    }
}
