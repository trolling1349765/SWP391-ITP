package fpt.swp.springmvctt.itp.entity.enums;

public enum ProductType {
    // Viễn thông
    VIETTEL("Thẻ Viettel"),
    MOBIFONE("Thẻ Mobifone"),
    VINAPHONE("Thẻ Vinaphone"),
    VIETTEL_DATA("Gói data Viettel"),
    MOBIFONE_DATA("Gói data Mobifone"),
    VINAPHONE_DATA("Gói data Vinaphone"),
    
    // Tài khoản số
    EMAIL("Tài khoản email"),
    SOCIAL("Tài khoản social media"),
    STREAMING("Tài khoản streaming"),
    APP("Tài khoản ứng dụng"),
    
    // Quà tặng & Voucher
    GIFT("Thẻ quà tặng"),
    VOUCHER("Voucher"),
    COUPON("Coupon"),
    PROMO("Mã khuyến mãi"),
    
    // Phần mềm & License
    SOFTWARE("Key phần mềm"),
    LICENSE("License key"),
    ACTIVATION("Mã kích hoạt"),
    SUBSCRIPTION("Subscription"),
    
    // Gaming
    GAME_ACC("Tài khoản game"),
    GAME_ITEM("Item game"),
    GAME_CURRENCY("Tiền tệ game"),
    GAME_CODE("Gift code game"),
    
    // Khác
    OTHER("Khác");



    
    private final String displayName;
    
    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
