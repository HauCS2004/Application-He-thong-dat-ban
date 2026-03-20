package connectDB;

import Entity.NhanVien;

/**
 * SessionManager - Singleton quản lý session đang login
 * Sau GĐ1: vaiTro lấy từ TaiKhoan (NhanVien.getTaiKhoan())
 */
public class SessionManager {
    private static SessionManager instance = null;
    private static NhanVien currentUser = null;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null)
            instance = new SessionManager();
        return instance;
    }

    /** Lưu user sau khi login thành công */
    public static void login(NhanVien user) {
        currentUser = user;
        String vaiTro = (user.getTaiKhoan() != null) ? user.getTaiKhoan().getVaiTro() : "Nhân viên";
        System.out.println("✅ User logged in: " + user.getTenNV() + " (" + vaiTro + ")");
    }

    /** Logout và xóa session */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("User logged out: " + currentUser.getTenNV());
            currentUser = null;
        }
    }

    /** Lấy user hiện tại */
    public static NhanVien getCurrentUser() {
        return currentUser;
    }

    /** Check đã login chưa */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Check có phải Quản lý không */
    public static boolean isManager() {
        return currentUser != null && currentUser.isQuanLy();
    }

    /**
     * Check quyền truy cập
     * 
     * @param requiredRole "Quản lý" hoặc "Nhân viên"
     */
    public static boolean hasPermission(String requiredRole) {
        if (currentUser == null)
            return false;
        if (currentUser.isQuanLy())
            return true; // Quản lý có tất cả quyền
        return "Nhân viên".equals(requiredRole);
    }

    /** Lấy tên hiển thị của user */
    public static String getDisplayName() {
        if (currentUser == null)
            return "Guest";
        String vaiTro = (currentUser.getTaiKhoan() != null)
                ? currentUser.getTaiKhoan().getVaiTro()
                : "Nhân viên";
        return currentUser.getTenNV() + " (" + vaiTro + ")";
    }
}
