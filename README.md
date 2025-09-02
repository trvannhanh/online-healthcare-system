
<h1 align="center">
  <br>
  <a href="https://github.com/trvannhanh/online-healthcare-system"><img src="https://res.cloudinary.com/dqpkxxzaf/image/upload/v1756790148/healcarelogo_jfh3x6.png" alt="HealCare" width="200"></a>
  <br>
  HealCare
  <br>
</h1>

<h4 align="center">
  Ứng dụng web đặt lịch khám sức khỏe trực tuyến được phát triển với SpringMVC, Hibernate, MySQL, Thymeleaf, ReactJS và Bootstrap. Hệ thống hỗ trợ quản lý hồ sơ bệnh nhân, đặt lịch khám, tư vấn trực tuyến, thanh toán dịch vụ và nhắc nhở tự động qua email/notification.
</h4>

<p align="center">
  <a href="#tính-năng-chính">Tính năng chính</a> •
  <a href="#-công-nghệ-sử-dụng">Công nghệ sử dụng</a> •
  <a href="#-kiến-trúc--nguyên-tắc">Kiến trúc & Nguyên tắc</a> •
  <a href="#-yêu-cầu-cài-đặt">Yêu cầu cài đặt</a> •
  <a href="#how-to-use">Cách chạy project</a> •
</p>

---

## 🚀 Tính năng chính

- 🔑 **Xác thực & phân quyền**: đăng nhập/đăng ký (bệnh nhân, bác sĩ, quản trị viên).  
- 📋 **Quản lý hồ sơ sức khỏe**: lưu trữ kết quả khám, đơn thuốc, bệnh án.  
- 🗓️ **Đặt lịch khám**: chọn bác sĩ, chuyên khoa, bệnh viện; nhận email xác nhận.  
- 🎥 **Tư vấn trực tuyến**: tích hợp video call qua WebRTC/Jitsi.  
- 💳 **Thanh toán dịch vụ**: hỗ trợ VNPay, MoMo, Stripe.  
- ⭐ **Đánh giá & phản hồi**: bệnh nhân có thể đánh giá bác sĩ và dịch vụ.  
- 📊 **Thống kê & báo cáo**: quản trị xem số lượng khám, doanh thu, phản hồi.  
- 🔔 **Nhắc nhở & thông báo**: gửi email/push notification.  

---

## 🛠 Công nghệ sử dụng

- **Backend**: SpringMVC, Hibernate, Spring Security (JWT Authentication)  
- **Frontend**: ReactJS, Bootstrap, Axios  
- **Database**: MySQL  
- **Template Engine (Admin panel)**: Thymeleaf  
- **Cloud Storage**: Cloudinary (lưu trữ ảnh bệnh nhân, hồ sơ y tế)  
- **Realtime**: Jitsi, FireBase (tư vấn trực tuyến)  

---

## 🔑 Kiến trúc & Nguyên tắc

- Sử dụng **Dependency Injection (DI)** để tách biệt các thành phần, giảm sự phụ thuộc lẫn nhau và tăng khả năng mở rộng, kiểm thử.  
- Tuân theo các nguyên tắc **SOLID** nhằm đảm bảo mã nguồn rõ ràng, dễ bảo trì:  
  - **S**: Single Responsibility Principle – Mỗi lớp chỉ đảm nhận một trách nhiệm duy nhất.  
  - **O**: Open/Closed Principle – Dễ mở rộng tính năng, hạn chế chỉnh sửa trực tiếp mã gốc.  
  - **L**: Liskov Substitution Principle – Có thể thay thế đối tượng bằng lớp con mà không phá vỡ tính đúng đắn.  
  - **I**: Interface Segregation Principle – Chia nhỏ interface, tránh tạo interface quá lớn.  
  - **D**: Dependency Inversion Principle – Lớp cấp cao không phụ thuộc trực tiếp vào lớp cấp thấp, mà thông qua abstraction (interface).  
- Thiết kế theo mô hình **Modular Monolith** với các tầng rõ ràng: Controller, Service, Repository.  

---

## ⚙️ Yêu cầu cài đặt

- JDK 17+  
- Apache Maven 3.8+  
- MySQL 8.0+  
- Node.js 18+  
- IDE: IntelliJ IDEA / Eclipse / VS Code  

## How To Use

Từ command line:

```bash
# Clone this repository
$ git clone https://github.com/your-username/wordsoul

# Go into the repository
$ cd wordsoul

# Backend
$ cd wordsoulapi
$ mvn clean install
$ mvn spring-boot:run

# Frontend
$ cd wordsoul-app
$ npm install
$ npm start
```

---

> GitHub [@trvannhanh](https://github.com/trvannhanh) &nbsp;&middot;&nbsp;

