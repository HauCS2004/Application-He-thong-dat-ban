/* ===================================================
 * DATABASE QUẢN LÝ NHÀ HÀNG - PHIÊN BẢN FULL OPTION
 * Hỗ trợ: Ghép bàn, Đặt bàn, VIP, Tích điểm
 * =================================================== */

USE master
GO

-- 1. XÓA DB CŨ NẾU CÓ (Làm sạch hệ thống)
IF EXISTS (SELECT * FROM sys.databases WHERE name = 'QuanLyNhaHang')
BEGIN
    ALTER DATABASE QuanLyNhaHang SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyNhaHang;
END
GO

-- 2. TẠO DATABASE MỚI
CREATE DATABASE QuanLyNhaHang
GO

USE QuanLyNhaHang
GO

/* ==================== TẠO BẢNG ==================== */

-- 1. Bảng Nhân Viên
CREATE TABLE NhanVien (
    MaNV NVARCHAR(20) PRIMARY KEY,
    TenNV NVARCHAR(50) NOT NULL,
    MatKhau NVARCHAR(50) NOT NULL,
    ChucVu NVARCHAR(20) DEFAULT N'Nhân viên'
)
GO

-- 2. Bảng Khu Vực (Tầng 1, Tầng 2...)
CREATE TABLE KhuVuc (
    MaKV VARCHAR(10) PRIMARY KEY,
    TenKV NVARCHAR(50) NOT NULL
)
GO

-- 3. Bảng Bàn (Quan trọng: Có Số Ghế & Mã Bàn Gộp)
CREATE TABLE Ban (
    MaBan VARCHAR(10) PRIMARY KEY,
    TenBan NVARCHAR(50) NOT NULL,
    TrangThai NVARCHAR(20) DEFAULT N'Trống', -- Trống, Có Khách, Đã Đặt, Đang Gộp
    MaKV VARCHAR(10) NOT NULL,
    SoGhe INT DEFAULT 4,             -- [MỚI] Sức chứa
    MaBanGop VARCHAR(10) NULL,       -- [MỚI] Nếu đang gộp thì lưu mã bàn chính vào đây
    
    FOREIGN KEY (MaKV) REFERENCES KhuVuc(MaKV)
    -- Tự tham chiếu để biết bàn này gộp vào bàn nào
    -- FOREIGN KEY (MaBanGop) REFERENCES Ban(MaBan) (Tạm bỏ qua constraint này để tránh lỗi vòng lặp khi insert, xử lý bằng code)
)
GO

-- 4. Bảng Loại Món
CREATE TABLE LoaiMon (
    MaLoai VARCHAR(10) PRIMARY KEY,
    TenLoai NVARCHAR(50) NOT NULL
)
GO

-- 5. Bảng Món Ăn
CREATE TABLE MonAn (
    MaMon VARCHAR(10) PRIMARY KEY,
    TenMon NVARCHAR(100) NOT NULL,
    DonViTinh NVARCHAR(20),
    DonGia FLOAT DEFAULT 0,
    HinhAnh NVARCHAR(200) DEFAULT 'default.png',
    MaLoai VARCHAR(10) NOT NULL,
    FOREIGN KEY (MaLoai) REFERENCES LoaiMon(MaLoai)
)
GO

-- 6. Bảng Khách Hàng (Tích điểm VIP)
CREATE TABLE KhachHang (
    SoDienThoai VARCHAR(20) PRIMARY KEY, 
    TenKhach NVARCHAR(50) NOT NULL,
    DiemTichLuy INT DEFAULT 0          -- [MỚI] Điểm để tính hạng VIP
)
GO

-- 7. Bảng Đặt Bàn (Booking)
CREATE TABLE DatBan (
    MaDat INT IDENTITY(1,1) PRIMARY KEY,
    MaBan VARCHAR(10) NOT NULL,
    TenKhachDat NVARCHAR(50),
    SDT VARCHAR(20),
    ThoiGianDat DATETIME,
    SoLuongKhach INT DEFAULT 1,
    GhiChu NVARCHAR(200),
    FOREIGN KEY (MaBan) REFERENCES Ban(MaBan)
)
GO

-- 8. Bảng Hóa Đơn (Lưu vết khách hàng để tích điểm)
CREATE TABLE HoaDon (
    MaHD INT IDENTITY(1,1) PRIMARY KEY,
    NgayTao DATETIME DEFAULT GETDATE(),
    TongTien FLOAT DEFAULT 0,
    TrangThai INT DEFAULT 0, -- 0: Chưa TT, 1: Đã TT
    MaBan VARCHAR(10) NOT NULL,
    SoLuongKhach INT DEFAULT 1,
    SDT_Khach VARCHAR(20) NULL, -- [MỚI] Link tới KhachHang (Có thể null nếu khách vãng lai)
    GhiChu NVARCHAR(100) NULL,  -- Lưu tên khách nếu không có SĐT
    
    FOREIGN KEY (MaBan) REFERENCES Ban(MaBan),
    FOREIGN KEY (SDT_Khach) REFERENCES KhachHang(SoDienThoai)
)
GO

-- 9. Bảng Chi Tiết Hóa Đơn
CREATE TABLE ChiTietHoaDon (
    MaHD INT,
    MaMon VARCHAR(10),
    SoLuong INT DEFAULT 1,
    DonGia FLOAT,
    
    PRIMARY KEY (MaHD, MaMon),
    FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD),
    FOREIGN KEY (MaMon) REFERENCES MonAn(MaMon)
)
GO

/* ==================== THÊM DỮ LIỆU MẪU ==================== */

-- 1. Nhân viên
INSERT INTO NhanVien VALUES 
('admin', N'Quản Lý', '123', N'Quản lý'),
('nv01', N'Nhân Viên A', '123', N'Nhân viên')

-- 2. Khu vực
INSERT INTO KhuVuc VALUES ('KV01', N'Tầng 1'), ('KV02', N'Tầng 2'), ('KV03', N'VIP')

-- 3. Bàn (Setup số ghế đa dạng để test chức năng check sức chứa)
-- Tầng 1: Bàn nhỏ 2-4 người
INSERT INTO Ban (MaBan, TenBan, TrangThai, MaKV, SoGhe) VALUES 
('B01', N'Bàn 1', N'Trống', 'KV01', 2),
('B02', N'Bàn 2', N'Trống', 'KV01', 2),
('B03', N'Bàn 3', N'Trống', 'KV01', 4),
('B04', N'Bàn 4', N'Trống', 'KV01', 4)

-- Tầng 2: Bàn gia đình 6-8 người
INSERT INTO Ban (MaBan, TenBan, TrangThai, MaKV, SoGhe) VALUES 
('B05', N'Bàn 5', N'Trống', 'KV02', 6),
('B06', N'Bàn 6', N'Trống', 'KV02', 6),
('B07', N'Bàn 7', N'Trống', 'KV02', 8),
('B08', N'Bàn 8', N'Trống', 'KV02', 8)

-- VIP: Bàn tiệc lớn
INSERT INTO Ban (MaBan, TenBan, TrangThai, MaKV, SoGhe) VALUES 
('VIP01', N'Bàn VIP 1', N'Trống', 'KV03', 12),
('VIP02', N'Bàn VIP 2', N'Trống', 'KV03', 20)

-- 4. Loại món
INSERT INTO LoaiMon VALUES 
('L01', N'Khai vị'), ('L02', N'Món chính'), 
('L03', N'Đồ uống'), ('L04', N'Tráng miệng')

-- 5. Món ăn
INSERT INTO MonAn (MaMon, TenMon, DonViTinh, DonGia, HinhAnh, MaLoai) VALUES 
('M01', N'Khoai tây chiên', N'Dĩa', 45000, 'khoai_tay.png', 'L01'),
('M02', N'Gỏi ngó sen tôm thịt', N'Dĩa', 85000, 'goi_sen.png', 'L01'),
('M03', N'Cơm chiên hải sản', N'Dĩa', 120000, 'com_chien.png', 'L02'),
('M04', N'Lẩu Thái Lan', N'Nồi', 250000, 'lau_thai.png', 'L02'),
('M05', N'Bò bít tết', N'Phần', 150000, 'bo_bit_tet.png', 'L02'),
('M06', N'Pepsi tươi', N'Ly', 20000, 'pepsi.png', 'L03'),
('M07', N'Trà đào cam sả', N'Ly', 45000, 'tra_dao.png', 'L03'),
('M08', N'Trái cây thập cẩm', N'Dĩa', 60000, 'trai_cay.png', 'L04')

-- 6. Khách hàng (Data test VIP)
INSERT INTO KhachHang VALUES 
('0909123456', N'Đại Gia Hạng Vàng', 600),   -- Điểm > 500 (Giảm 10%)
('0912345678', N'Khách Hạng Bạc', 250),      -- Điểm > 200 (Giảm 5%)
('0987654321', N'Khách Mới', 10)             -- Điểm thấp (Không giảm)

GO

PRINT N'=== TẠO CSDL THÀNH CÔNG (FULL TÍNH NĂNG) ==='