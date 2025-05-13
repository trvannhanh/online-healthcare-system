package com.can.repositories;

import com.can.pojo.Notifications;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author DELL
 */
public interface NotificationRepository {

    // Lọc thông báo theo các tiêu chí động (sử dụng Map)
    List<Notifications> getNotificationsByCriteria(Map<String, String> params) throws ParseException;

    // Tìm thông báo theo id
    Notifications getNotificationById(Integer id);

    // Tìm thông báo theo người dùng
    List<Notifications> getNotificationsByUserId(Integer userId);

    // Tìm thông báo theo ngày tạo
    List<Notifications> getNotificationsByCreateDate(String createAt);

    // Thêm một thông báo mới
    Notifications addNotification(Notifications notification);

    // Lấy thông báo của người dùng theo trạng thái xác minh (ví dụ: người dùng đã
    // xác minh)
    List<Notifications> getNotificationsByVerificationStatus(boolean isVerified, int page);

    // Lấy các thông báo của người dùng (có thể phân trang nếu cần)
    List<Notifications> getNotificationsByUserId(int userId, int page);

    // Tìm thông báo theo ngày gửi (sentAt)
    List<Notifications> getNotificationsByDateRange(Date startDate, Date endDate);

    // Kiểm tra thông báo đã tồn tại chưa (dựa trên ID hoặc nội dung)
    boolean isNotificationExist(int notificationId);

    // Cập nhật nội dung thông báo
    void updateNotificationMessage(int notificationId, String message);

    List<Notifications> getAllNotifications() throws ParseException;

    void deleteNotification(int id);

}
