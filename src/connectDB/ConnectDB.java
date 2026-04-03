package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    public static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() {
        // Thông số kết nối SQL Server
        String databaseName = "QuanLyNhaHang_V3";
        String user = "sa";
        String password = "sapassword"; // SỬA LẠI NẾU KHÁC

        // Dùng SQL Server Authentication (không cần DLL)
        String url = "jdbc:sqlserver://localhost:1433;"
                + "databaseName=" + databaseName + ";"
                + "user=" + user + ";password=" + password + ";"
                + "encrypt=true;trustServerCertificate=true;";

        // ℹ️ Nếu dùng SQL Server full (không phải Express):
        // String url = "jdbc:sqlserver://localhost:1433;..."

        try {
            // Đăng ký driver (phòng hờ)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Kết nối với Windows Authentication (không cần user/pass)
            con = DriverManager.getConnection(url);
            System.out.println("✅ Kết nối SQL Server thành công!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Lỗi kết nối SQL! Kiểm tra:");
            System.out.println("   - SQL Server có đang chạy?");
            System.out.println("   - Database 'QuanLyNhaHang' đã tạo chưa?");
            System.out.println("   - Instance name: localhost\\SQLEXPRESS");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("❌ Chưa add thư viện JDBC .jar vào project!");
        }
    }

    public static Connection getConnection() {
        return con;
    }

    // Hàm ngắt kết nối
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}