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
        String databaseName = "QuanLyNhaHang"; // Tên DB của bạn trong SQL Server
        String user = "sa";                    // Tài khoản mặc định là sa
        String password = "123";               // Mật khẩu bạn cài lúc cài SQL Server
        
        // Chuỗi kết nối chuẩn
        String url = "jdbc:sqlserver://localhost:1433;"
                   + "databaseName=" + databaseName + ";"
                   + "encrypt=true;trustServerCertificate=true;";

        try {
            // Đăng ký driver (phòng hờ)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
            
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Kết nối SQL Server thành công!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Lỗi kết nối SQL! Sai tên DB hoặc sai mật khẩu.");
        } catch (ClassNotFoundException e) {
             e.printStackTrace();
             System.out.println("Chưa add thư viện JDBC .jar vào Eclipse!");
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