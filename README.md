# Ứng Dụng Hệ Thống Đặt Bàn Nhà Hàng (Java Swing)

Đây là dự án ứng dụng Quản lý - Phục vụ nhà hàng được viết bằng Java (Java Swing) kết hợp cơ sở dữ liệu SQL Server. Ứng dụng hỗ trợ quy trình đầy đủ từ việc khách hàng gọi món, đặt bàn, xem sơ đồ bàn cho đến thanh toán xuất hóa đơn và quản lý các dữ liệu như nhân viên, khách hàng thành viên.

## 📁 Cấu Trúc Thư Mục Dự Án

Dự án được tổ chức theo mô hình Layered Architecture (MVC / Data Access Object).

-   **`src/Entity/`**
    Chứa các lớp Java định nghĩa thực thể (Model), ánh xạ 1-1 với cấu trúc các bảng trong cơ sở dữ liệu SQL Server. 
    Các thực thể chính: `Ban`, `BangGia`, `ChiTietBangGia`, `ChiTietHoaDon`, `DatBan`, `HoaDon`, `KhachHang`, `KhuyenMai`, `LoaiMon`, `MonAn`, `NhanVien`, `TaiKhoan`.

-   **`src/DAO/`**
    (Data Access Object): Nơi xử lý trực tiếp mọi tương tác với Cơ sở dữ liệu SQL (SELECT, INSERT, UPDATE, DELETE).
    Ví dụ: `BanDAO.java`, `HoaDonDAO.java`, `DatBanDAO.java`, `ThongKeDAO.java`,...

-   **`src/GUI/`**
    (Graphical User Interface): Quản lý toàn bộ giao diện phần mềm. Màn hình chính và các màn hình module đều nằm ở đây.
    -   `components/`: Các thành phần giao diện được tùy biến, có thể tái sử dụng.
    -   `utils/`: Tiện ích vẽ giao diện hoặc bo góc chung.

-   **`src/connectDB/`**
    Xử lý cấu hình kết nối tới SQL Server (`ConnectDB.java`) và quản lý session của user đang đăng nhập (`SessionManager.java`).

-   **`libs/`**
    Thư mục chứa các tệp phụ thuộc `.jar`.
    -   `mssql-jdbc-13.2.1.jre11.jar`: Thư viện kết nối CSDL SQL Server.
    -   `flatlaf-3.7.jar`: Thư viện giao diện (Look And Feel) hiện đại cho Java Swing.
    -   `jcalendar-1.4.jar`: Thư viện bộ chọn ngày/tháng JDateChooser.
    -   `itextpdf-5.5.13.3.jar`: Thư viện để in và xuất Hóa đơn ra file chuẩn PDF.

-   **`bin/`**
    Nơi chứa toàn bộ mã máy byte-code (`.class`) sau khi đã biên dịch project.

---

## 🖥️ Các Giao Diện Màn Hình (GUI) Chính

Dự án có những màn hình giao diện quan trọng phối hợp với nhau nhằm tạo ra hệ sinh thái quản lý 360 độ:

| Tên File | Vai trò & Chức năng |
| :--- | :--- |
| `MainLayout.java` | Khung sườn tổng thể của ứng dụng, chứa Sidebar bên trái và một khoảng trống hiển thị nội dung bên phải. |
| `ManHinhDangNhap.java` | Cổng xác thực người dùng. Kiểm tra tài khoản trong Database và lưu phiên bằng `SessionManager`. |
| `ManHinhTrangChu.java` | Dashboard chính khởi diện lúc mới vào, hiển thị nhanh các số liệu tóm tắt trong ngày/tháng. |
| `ManHinhPhucVu.java` | Trái tim của nghiệp vụ tại quán: Sơ đồ hiển thị các bàn, màu xanh đỏ, cho phép nhận bàn trống, phục vụ khách vãng lai, chuyển bàn, hay ghép nhiều bàn. |
| `ManHinhDatBanV2.java` | Quản lý sổ đặt bàn của khách, xem lịch đặt tới, thực hiện chức năng đặt rảnh. |
| `ManHinhGoiMon.java` | Giao diện cho nhân viên thêm bớt thức ăn, nước uống (Menu) vào hóa đơn của từng bàn. |
| `ManHinhHoaDon.java` | Quản lý tiền bạc: Chi tiết thanh toán, hỗ trợ giảm giá qua thẻ VIP/Voucher, tính thuế, in/xuất file hóa đơn (PDF), và xem lại lịch sử các hóa đơn. |
| `ManHinhKhachHang.java` | Quản lý khách hàng thân thiết, hạng VIP. |
| `ManHinhNhanVien.java` | Quản lý hồ sơ, thêm/sửa/xóa thông tin các nhân sự nội bộ trong hệ thống. |
| `ManHinhThongKe.java` | Thống kê biểu đồ trực quan, cho phép nhà quản trị nắm bắt doanh thu theo giai đoạn. |

Các Dialog (Cửa sổ bật lên nhỏ hơn): 
Bên cạnh đó, ứng dụng sử dụng các `*Dialog.java` như `GanKhachDialog.java`, `BookingFormDialog.java`, và giao diện hóa đơn xem trước của Print Preview để bổ trợ việc thao tác nhanh không cần chuyển trang lớn.
