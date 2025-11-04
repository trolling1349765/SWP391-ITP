# Setup Hướng Dẫn

## Cấu hình Local Environment

Để chạy project ở local, bạn cần tạo file `application-local.properties`:

### Bước 1: Tạo file
```bash
cd src/main/resources
cp application-local.properties.example application-dev.properties
```

### Bước 2: Điền thông tin
Mở file `application-local.properties` và điền các thông tin:

```properties
# Google OAuth (lấy từ Google Cloud Console)
google.clientSecret=your_google_client_secret_here
google.clientId=your_google_client_id_here
google.redirectUri=http://localhost:8080/itp/oauth2/callback/google

# Database
spring.datasource.password=your_mysql_password

# Cloudinary (lấy từ Cloudinary Dashboard)
cloudinary.cloud_name=your_cloud_name
cloudinary.api_key=your_api_key
cloudinary.api_secret=your_api_secret

# Gmail SMTP
spring.mail.username=your_email@gmail.com
spring.mail.password=your_gmail_app_password
```

### Bước 3: Chạy project
```bash
./mvnw spring-boot:run
```

## ⚠️ LƯU Ý
- **KHÔNG BAO GIỜ** commit file `application-dev.properties`
- File này đã được thêm vào `.gitignore`
- Liên hệ team leader để lấy credentials cho development

