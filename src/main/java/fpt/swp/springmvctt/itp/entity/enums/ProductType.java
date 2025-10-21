package fpt.swp.springmvctt.itp.entity.enums;

public enum ProductType {
    PHONE_CARD("Thẻ điện thoại"),
    EMAIL_ACCOUNT("Tài khoản email"),
    GIFT_CARD("Thẻ quà tặng"),
    SOFTWARE_KEY("Key phần mềm"),
    OTHER("Khác");
    
    private final String displayName;
    
    ProductType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
