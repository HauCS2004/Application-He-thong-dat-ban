package connectDB;

import Entity.NhanVien;

/**
 * SessionManager - Quản lý session người dùng hiện tại
 * Singleton pattern để lưu user đang login
 */
public class SessionManager {
    private static SessionManager instance = null;
    private static NhanVien currentUser = null;

    // Private constructor (Singleton)
    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Lưu user sau khi login thành công
     */
    public static void login(NhanVien user) {
        currentUser = user;
        System.out.println("✅ User logged in: " + user.getTenNV() + " (" + user.getChucVu() + ")");
    }

    /**
     * Logout và xóa session
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("👋 User logged out: " + currentUser.getTenNV());
            currentUser = null;
        }
    }

    /**
     * Lấy user hiện tại
     */
    public static NhanVien getCurrentUser() {
        return currentUser;
    }

    /**
     * Check xem user đã login chưa
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check xem user có phải là Quản lý không
     */
    public static boolean isManager() {
        if (currentUser == null)
            return false;
        return currentUser.getChucVu().equals("Quản lý");
    }

    /**
     * Check xem user có quyền truy cập chức năng không
     * 
     * @param requiredRole - "Quản lý" hoặc "Nhân viên"
     */
    public static boolean hasPermission(String requiredRole) {
        if (currentUser == null)
            return false;

        // Quản lý có tất cả quyền
        if (currentUser.getChucVu().equals("Quản lý")) {
            return true;
        }

        // Nhân viên chỉ có quyền của nhân viên
        return requiredRole.equals("Nhân viên");
    }

    /**
     * Lấy tên hiển thị của user
     */
    public static String getDisplayName() {
        if (currentUser == null)
            return "Guest";
        return currentUser.getTenNV() + " (" + currentUser.getChucVu() + ")";
    }
}
