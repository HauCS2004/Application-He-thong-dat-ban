/* ===================================================
 * DATABASE QUẢN LÝ NHÀ HÀNG - PHIÊN BẢN NÂNG CAP 2.0
 * Hỗ trợ: Ghép bàn, Đặt bàn theo khung giờ, VIP 4 hạng, 
 *         Tích điểm, Khuyến mãi, Lịch sử phục vụ
 * Ngày tạo: 2026-01-28
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
    ChucVu NVARCHAR(20) DEFAULT N'Nhân viên',
    SoDienThoai VARCHAR(20) NULL,
    Email NVARCHAR(100) NULL,
    NgayVaoLam DATE DEFAULT GETDATE()
)
GO

-- 2. Bảng Khu Vực (Tầng 1, Tầng 2...)
CREATE TABLE KhuVuc (
    MaKV VARCHAR(10) PRIMARY KEY,
    TenKV NVARCHAR(50) NOT NULL,
    MoTa NVARCHAR(200) NULL
)
GO

-- 3. Bảng Bàn (Nâng cấp: Thêm tracking thời gian)
CREATE TABLE Ban (
    MaBan VARCHAR(10) PRIMARY KEY,
    TenBan NVARCHAR(50) NOT NULL,
    TrangThai NVARCHAR(20) DEFAULT N'Trống', -- Trống, Có Khách, Đã Đặt, Đang Gộp
    MaKV VARCHAR(10) NOT NULL,
    SoGhe INT DEFAULT 4,
    MaBanGop VARCHAR(10) NULL,
    ThoiGianCapNhat DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (MaKV) REFERENCES KhuVuc(MaKV)
)
GO

-- 4. Bảng Loại Món
CREATE TABLE LoaiMon (
    MaLoai VARCHAR(10) PRIMARY KEY,
    TenLoai NVARCHAR(50) NOT NULL,
    MoTa NVARCHAR(200) NULL
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
    TrangThai NVARCHAR(20) DEFAULT N'Còn món', -- Còn món, Hết món, Ngừng phục vụ
    FOREIGN KEY (MaLoai) REFERENCES LoaiMon(MaLoai)
)
GO

-- 6. Bảng Khách Hàng (NÂNG CẤP: VIP 4 hạng + tracking chi tiết)
CREATE TABLE KhachHang (
    SoDienThoai VARCHAR(20) PRIMARY KEY, 
    TenKhach NVARCHAR(50) NOT NULL,
    Email NVARCHAR(100) NULL,
    NgaySinh DATE NULL,
    DiemTichLuy INT DEFAULT 0,
    HangVIP NVARCHAR(20) DEFAULT N'Đồng', -- Đồng (0-199), Bạc (200-499), Vàng (500-999), Kim cương (1000+)
    NgayTao DATETIME DEFAULT GETDATE(),
    LanGiaoDichCuoi DATETIME NULL,
    TongChiTieu FLOAT DEFAULT 0,
    GhiChu NVARCHAR(200) NULL
)
GO

-- 7. Bảng Đặt Bàn (NÂNG CẤP CHÍNH: Khung giờ + Trạng thái chi tiết)
CREATE TABLE DatBan (
    MaDat INT IDENTITY(1,1) PRIMARY KEY,
    MaBan VARCHAR(10) NOT NULL,
    TenKhachDat NVARCHAR(50),
    SDT VARCHAR(20),
    ThoiGianBatDau DATETIME NOT NULL,
    ThoiGianKetThuc DATETIME NOT NULL,
    SoLuongKhach INT DEFAULT 1,
    TrangThai NVARCHAR(20) DEFAULT N'Chờ xác nhận', -- Chờ xác nhận, Đã nhận bàn, Đã hủy, Hoàn thành
    TienCoc FLOAT DEFAULT 0,
    GhiChu NVARCHAR(200),
    NgayTao DATETIME DEFAULT GETDATE(),
    MaHD INT NULL, -- Link với hóa đơn thực tế khi khách đến
    
    FOREIGN KEY (MaBan) REFERENCES Ban(MaBan),
    CONSTRAINT CK_ThoiGian CHECK (ThoiGianKetThuc > ThoiGianBatDau)
)
GO

-- 8. Bảng Hóa Đơn (Nâng cấp: Link với khách hàng để tích điểm)
CREATE TABLE HoaDon (
    MaHD INT IDENTITY(1,1) PRIMARY KEY,
    NgayTao DATETIME DEFAULT GETDATE(),
    TongTien FLOAT DEFAULT 0,
    TrangThai INT DEFAULT 0, -- 0: Chưa thanh toán, 1: Đã thanh toán
    MaBan VARCHAR(10) NOT NULL,
    SoLuongKhach INT DEFAULT 1,
    SDT_Khach VARCHAR(20) NULL,
    GhiChu NVARCHAR(100) NULL,
    MaNV NVARCHAR(20) NULL, -- Nhân viên phục vụ
    
    FOREIGN KEY (MaBan) REFERENCES Ban(MaBan),
    FOREIGN KEY (SDT_Khach) REFERENCES KhachHang(SoDienThoai),
    FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
)
GO

-- 9. Bảng Chi Tiết Hóa Đơn
CREATE TABLE ChiTietHoaDon (
    MaHD INT,
    MaMon VARCHAR(10),
    SoLuong INT DEFAULT 1,
    DonGia FLOAT,
    GhiChu NVARCHAR(100) NULL, -- Ghi chú đặc biệt của món (VD: ít cay, không hành...)
    
    PRIMARY KEY (MaHD, MaMon),
    FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD),
    FOREIGN KEY (MaMon) REFERENCES MonAn(MaMon)
)
GO

-- 10. [MỚI] Bảng Khuyến Mãi
CREATE TABLE KhuyenMai (
    MaKM VARCHAR(20) PRIMARY KEY,
    TenKM NVARCHAR(100) NOT NULL,
    LoaiKM NVARCHAR(20) CHECK (LoaiKM IN (N'Giảm %', N'Giảm tiền', N'Tặng món')),
    GiaTri FLOAT,
    DieuKienToiThieu FLOAT DEFAULT 0, -- Hóa đơn tối thiểu để áp dụng
    NgayBatDau DATETIME,
    NgayKetThuc DATETIME,
    TrangThai NVARCHAR(20) DEFAULT N'Đang hoạt động',
    HangVIPApDung NVARCHAR(20) NULL, -- NULL = tất cả, hoặc chỉ định: Bạc, Vàng, Kim cương
    
    CONSTRAINT CK_KM_ThoiGian CHECK (NgayKetThuc > NgayBatDau)
)
GO

-- 11. [MỚI] Bảng liên kết: Hóa đơn - Khuyến mãi
CREATE TABLE HoaDon_KhuyenMai (
    MaHD INT,
    MaKM VARCHAR(20),
    GiamGia FLOAT,
    
    PRIMARY KEY (MaHD, MaKM),
    FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD),
    FOREIGN KEY (MaKM) REFERENCES KhuyenMai(MaKM)
)
GO

-- 12. [MỚI] Bảng Lịch Sử Bàn (Tracking hiệu suất)
CREATE TABLE LichSuBan (
    MaLichSu INT IDENTITY(1,1) PRIMARY KEY,
    MaBan VARCHAR(10) NOT NULL,
    ThoiGianBatDau DATETIME NOT NULL,
    ThoiGianKetThuc DATETIME NULL,
    SoLuongKhach INT,
    DoanhThu FLOAT DEFAULT 0,
    MaHD INT NULL,
    
    FOREIGN KEY (MaBan) REFERENCES Ban(MaBan),
    FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD)
)
GO

/* ==================== TRIGGERS ==================== */

-- Trigger 1: Tự động cập nhật hạng VIP khi điểm thay đổi
CREATE TRIGGER TR_CapNhatHangVIP
ON KhachHang
AFTER UPDATE
AS
BEGIN
    IF UPDATE(DiemTichLuy)
    BEGIN
        UPDATE KhachHang
        SET HangVIP = CASE 
            WHEN DiemTichLuy >= 1000 THEN N'Kim cương'
            WHEN DiemTichLuy >= 500 THEN N'Vàng'
            WHEN DiemTichLuy >= 200 THEN N'Bạc'
            ELSE N'Đồng'
        END
        WHERE SoDienThoai IN (SELECT SoDienThoai FROM inserted)
    END
END
GO

-- Trigger 2: Cập nhật Foreign Key cho DatBan -> HoaDon (sau khi tạo HoaDon)
CREATE TRIGGER TR_LinkDatBanHoaDon
ON HoaDon
AFTER INSERT
AS
BEGIN
    -- Tự động link đặt bàn với hóa đơn nếu có
    UPDATE DatBan
    SET MaHD = i.MaHD,
        TrangThai = N'Hoàn thành'
    FROM DatBan db
    INNER JOIN inserted i ON db.MaBan = i.MaBan
    WHERE db.TrangThai = N'Đã nhận bàn'
        AND GETDATE() BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc
END
GO

-- Trigger 3: Tự động tính tổng tiền hóa đơn khi thêm/sửa chi tiết
CREATE TRIGGER TR_TinhTongTien
ON ChiTietHoaDon
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    -- Update tổng tiền cho các hóa đơn bị ảnh hưởng
    UPDATE HoaDon
    SET TongTien = (
        SELECT ISNULL(SUM(SoLuong * DonGia), 0)
        FROM ChiTietHoaDon
        WHERE MaHD = HoaDon.MaHD
    )
    WHERE MaHD IN (
        SELECT MaHD FROM inserted
        UNION
        SELECT MaHD FROM deleted
    )
END
GO

/* ==================== STORED PROCEDURES ==================== */

-- SP 1: Kiểm tra xung đột đặt bàn
CREATE PROCEDURE SP_KiemTraDatBan
    @MaBan VARCHAR(10),
    @ThoiGianBatDau DATETIME,
    @ThoiGianKetThuc DATETIME,
    @MaDatHienTai INT = NULL -- Cho phép kiểm tra khi update booking
AS
BEGIN
    SELECT 
        COUNT(*) AS SoLuongXungDot
    FROM DatBan
    WHERE MaBan = @MaBan
        AND (@MaDatHienTai IS NULL OR MaDat != @MaDatHienTai)
        AND TrangThai IN (N'Chờ xác nhận', N'Đã nhận bàn')
        AND (
            (@ThoiGianBatDau BETWEEN ThoiGianBatDau AND ThoiGianKetThuc)
            OR (@ThoiGianKetThuc BETWEEN ThoiGianBatDau AND ThoiGianKetThuc)
            OR (ThoiGianBatDau BETWEEN @ThoiGianBatDau AND @ThoiGianKetThuc)
        )
END
GO

-- SP 2: Gợi ý bàn phù hợp
CREATE PROCEDURE SP_GoiYBan
    @SoKhach INT,
    @ThoiGianBatDau DATETIME,
    @ThoiGianKetThuc DATETIME,
    @MaKV VARCHAR(10) = NULL
AS
BEGIN
    SELECT TOP 10
        b.MaBan, 
        b.TenBan, 
        b.SoGhe, 
        kv.TenKV,
        ABS(b.SoGhe - @SoKhach) AS ChenhLech
    FROM Ban b
    JOIN KhuVuc kv ON b.MaKV = kv.MaKV
    WHERE b.SoGhe >= @SoKhach
        AND (@MaKV IS NULL OR b.MaKV = @MaKV)
        AND b.TrangThai NOT IN (N'Đang Gộp') -- Không gợi ý bàn đang gộp
        AND b.MaBan NOT IN (
            -- Loại bỏ bàn đã được đặt trong khung giờ này
            SELECT MaBan FROM DatBan
            WHERE TrangThai IN (N'Chờ xác nhận', N'Đã nhận bàn')
                AND (
                    (@ThoiGianBatDau BETWEEN ThoiGianBatDau AND ThoiGianKetThuc)
                    OR (@ThoiGianKetThuc BETWEEN ThoiGianBatDau AND ThoiGianKetThuc)
                    OR (ThoiGianBatDau BETWEEN @ThoiGianBatDau AND @ThoiGianKetThuc)
                )
        )
    ORDER BY ChenhLech, b.SoGhe -- Ưu tiên bàn vừa vặn nhất
END
GO

-- SP 3: Tích điểm cho khách hàng
CREATE PROCEDURE SP_TichDiem
    @MaHD INT,
    @SDT VARCHAR(20)
AS
BEGIN
    DECLARE @TongTien FLOAT
    SELECT @TongTien = TongTien FROM HoaDon WHERE MaHD = @MaHD
    
    -- Quy tắc: Mỗi 10,000đ = 1 điểm
    DECLARE @DiemThem INT = FLOOR(@TongTien / 10000)
    
    IF EXISTS (SELECT 1 FROM KhachHang WHERE SoDienThoai = @SDT)
    BEGIN
        UPDATE KhachHang
        SET DiemTichLuy = DiemTichLuy + @DiemThem,
            LanGiaoDichCuoi = GETDATE(),
            TongChiTieu = TongChiTieu + @TongTien
        WHERE SoDienThoai = @SDT
        
        PRINT N'Đã tích ' + CAST(@DiemThem AS VARCHAR) + N' điểm cho khách hàng ' + @SDT
    END
    ELSE
    BEGIN
        PRINT N'Không tìm thấy khách hàng với SĐT: ' + @SDT
    END
END
GO

-- SP 4: Tính giảm giá VIP theo hạng
CREATE PROCEDURE SP_TinhGiamGiaVIP
    @SDT VARCHAR(20),
    @TongTien FLOAT,
    @GiamGia FLOAT OUTPUT
AS
BEGIN
    DECLARE @HangVIP NVARCHAR(20)
    
    SELECT @HangVIP = HangVIP 
    FROM KhachHang 
    WHERE SoDienThoai = @SDT
    
    SET @GiamGia = CASE 
        WHEN @HangVIP = N'Kim cương' THEN @TongTien * 0.15  -- Giảm 15%
        WHEN @HangVIP = N'Vàng' THEN @TongTien * 0.10         -- Giảm 10%
        WHEN @HangVIP = N'Bạc' THEN @TongTien * 0.05          -- Giảm 5%
        ELSE 0                                                 -- Đồng: không giảm
    END
END
GO

-- SP 5: Đặt bàn (tích hợp kiểm tra xung đột)
CREATE PROCEDURE SP_DatBan
    @MaBan VARCHAR(10),
    @TenKhachDat NVARCHAR(50),
    @SDT VARCHAR(20),
    @ThoiGianBatDau DATETIME,
    @ThoiGianKetThuc DATETIME,
    @SoLuongKhach INT,
    @GhiChu NVARCHAR(200) = NULL,
    @TienCoc FLOAT = 0,
    @KetQua INT OUTPUT,
    @ThongBao NVARCHAR(200) OUTPUT
AS
BEGIN
    -- Kiểm tra xung đột
    DECLARE @SoXungDot INT
    
    SELECT @SoXungDot = COUNT(*)
    FROM DatBan
    WHERE MaBan = @MaBan
        AND TrangThai IN (N'Chờ xác nhận', N'Đã nhận bàn')
        AND (
            (@ThoiGianBatDau BETWEEN ThoiGianBatDau AND ThoiGianKetThuc)
            OR (@ThoiGianKetThuc BETWEEN ThoiGianBatDau AND ThoiGianKetThuc)
            OR (ThoiGianBatDau BETWEEN @ThoiGianBatDau AND @ThoiGianKetThuc)
        )
    
    IF @SoXungDot > 0
    BEGIN
        SET @KetQua = 0
        SET @ThongBao = N'Bàn đã được đặt trong khung giờ này!'
        RETURN
    END
    
    -- Kiểm tra sức chứa
    DECLARE @SoGhe INT
    SELECT @SoGhe = SoGhe FROM Ban WHERE MaBan = @MaBan
    
    IF @SoLuongKhach > @SoGhe
    BEGIN
        SET @KetQua = 0
        SET @ThongBao = N'Số lượng khách vượt quá sức chứa bàn (' + CAST(@SoGhe AS VARCHAR) + N' ghế)'
        RETURN
    END
    
    -- Tạo booking
    INSERT INTO DatBan (MaBan, TenKhachDat, SDT, ThoiGianBatDau, ThoiGianKetThuc, 
                        SoLuongKhach, GhiChu, TienCoc, TrangThai)
    VALUES (@MaBan, @TenKhachDat, @SDT, @ThoiGianBatDau, @ThoiGianKetThuc, 
            @SoLuongKhach, @GhiChu, @TienCoc, N'Chờ xác nhận')
    
    SET @KetQua = 1
    SET @ThongBao = N'Đặt bàn thành công! Mã đặt: ' + CAST(SCOPE_IDENTITY() AS VARCHAR)
END
GO

-- SP 6: Thống kê doanh thu theo ngày
CREATE PROCEDURE SP_ThongKeDoanhThu
    @TuNgay DATE,
    @DenNgay DATE
AS
BEGIN
    SELECT 
        CAST(NgayTao AS DATE) AS Ngay,
        COUNT(MaHD) AS SoHoaDon,
        SUM(SoLuongKhach) AS TongKhach,
        SUM(TongTien) AS DoanhThu
    FROM HoaDon
    WHERE TrangThai = 1
        AND CAST(NgayTao AS DATE) BETWEEN @TuNgay AND @DenNgay
    GROUP BY CAST(NgayTao AS DATE)
    ORDER BY Ngay DESC
END
GO

/* ==================== INDEXES (Tối ưu Performance) ==================== */

-- Index cho tìm kiếm đặt bàn theo thời gian
CREATE INDEX IDX_DatBan_ThoiGian ON DatBan(ThoiGianBatDau, ThoiGianKetThuc, TrangThai)
CREATE INDEX IDX_DatBan_MaBan ON DatBan(MaBan, TrangThai)

-- Index cho khách hàng
CREATE INDEX IDX_KhachHang_Diem ON KhachHang(DiemTichLuy DESC)
CREATE INDEX IDX_KhachHang_Hang ON KhachHang(HangVIP)

-- Index cho hóa đơn
CREATE INDEX IDX_HoaDon_NgayTao ON HoaDon(NgayTao DESC)
CREATE INDEX IDX_HoaDon_TrangThai ON HoaDon(TrangThai)

-- Index cho món ăn
CREATE INDEX IDX_MonAn_Loai ON MonAn(MaLoai, TrangThai)

-- Index cho bàn
CREATE INDEX IDX_Ban_KhuVuc ON Ban(MaKV, TrangThai)

GO

/* ==================== VIEWS ==================== */

-- View 1: Trạng thái bàn thời gian thực
CREATE VIEW V_TrangThaiBanChiTiet AS
SELECT 
    b.MaBan,
    b.TenBan,
    b.TrangThai AS TrangThaiHienTai,
    b.SoGhe,
    kv.TenKV,
    db.MaDat,
    db.ThoiGianBatDau,
    db.ThoiGianKetThuc,
    db.TenKhachDat,
    db.SDT,
    db.TrangThai AS TrangThaiDatBan,
    CASE 
        WHEN b.TrangThai = N'Có Khách' THEN N'Đang phục vụ'
        WHEN db.TrangThai = N'Đã nhận bàn' AND GETDATE() < db.ThoiGianBatDau 
            THEN N'Đã nhận (chưa đến giờ)'
        WHEN db.TrangThai = N'Đã nhận bàn' AND GETDATE() BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc 
            THEN N'Đang trong giờ đặt'
        ELSE N'Trống'
    END AS TrangThaiChiTiet
FROM Ban b
JOIN KhuVuc kv ON b.MaKV = kv.MaKV
LEFT JOIN DatBan db ON b.MaBan = db.MaBan 
    AND db.TrangThai IN (N'Chờ xác nhận', N'Đã nhận bàn')
    AND GETDATE() BETWEEN DATEADD(HOUR, -1, db.ThoiGianBatDau) AND db.ThoiGianKetThuc
GO

-- View 2: Thống kê khách hàng VIP
CREATE VIEW V_ThongKeKhachVIP AS
SELECT 
    HangVIP,
    COUNT(*) AS SoLuong,
    SUM(TongChiTieu) AS TongDoanhThu,
    AVG(TongChiTieu) AS TrungBinhChiTieu,
    AVG(DiemTichLuy) AS TrungBinhDiem
FROM KhachHang
GROUP BY HangVIP
GO

/* ==================== DỮ LIỆU MẪU ==================== */

-- 1. Nhân viên
INSERT INTO NhanVien (MaNV, TenNV, MatKhau, ChucVu, SoDienThoai) VALUES 
('admin', N'Quản Lý Hệ Thống', '123', N'Quản lý', '0901234567'),
('nv01', N'Nguyễn Văn A', '123', N'Nhân viên', '0912345678'),
('nv02', N'Trần Thị B', '123', N'Nhân viên', '0923456789')

-- 2. Khu vực
INSERT INTO KhuVuc VALUES 
('KV01', N'Tầng 1', N'Khu vực dành cho khách đơn và đôi'),
('KV02', N'Tầng 2', N'Khu vực gia đình'),
('KV03', N'VIP', N'Phòng VIP và tiệc lớn')

-- 3. Bàn (Đa dạng kích cỡ)
INSERT INTO Ban (MaBan, TenBan, TrangThai, MaKV, SoGhe) VALUES 
-- Tầng 1: Bàn nhỏ
('B01', N'Bàn 1', N'Trống', 'KV01', 2),
('B02', N'Bàn 2', N'Trống', 'KV01', 2),
('B03', N'Bàn 3', N'Trống', 'KV01', 4),
('B04', N'Bàn 4', N'Trống', 'KV01', 4),
('B05', N'Bàn 5', N'Trống', 'KV01', 4),

-- Tầng 2: Bàn gia đình
('B06', N'Bàn 6', N'Trống', 'KV02', 6),
('B07', N'Bàn 7', N'Trống', 'KV02', 6),
('B08', N'Bàn 8', N'Trống', 'KV02', 8),
('B09', N'Bàn 9', N'Trống', 'KV02', 8),

-- VIP: Bàn tiệc
('VIP01', N'Phòng VIP 1', N'Trống', 'KV03', 12),
('VIP02', N'Phòng VIP 2', N'Trống', 'KV03', 20)

-- 4. Loại món
INSERT INTO LoaiMon VALUES 
('L01', N'Khai vị', N'Món mở đầu bữa ăn'),
('L02', N'Món chính', N'Món chính của bữa ăn'),
('L03', N'Đồ uống', N'Nước giải khát'),
('L04', N'Tráng miệng', N'Món ăn kết thúc bữa ăn')

-- 5. Món ăn
INSERT INTO MonAn (MaMon, TenMon, DonViTinh, DonGia, HinhAnh, MaLoai, TrangThai) VALUES 
('M01', N'Khoai tây chiên', N'Dĩa', 45000, 'khoaitaychien.jpg', 'L01', N'Còn món'),
('M02', N'Gỏi ngó sen tôm thịt', N'Dĩa', 85000, 'ngosentomthit.jpg', 'L01', N'Còn món'),
('M03', N'Salad trộn', N'Dĩa', 55000, 'salad.jpg', 'L01', N'Còn món'),
('M04', N'Cơm chiên hải sản', N'Dĩa', 120000, 'comchienhs.jpg', 'L02', N'Còn món'),
('M05', N'Lẩu Thái Lan', N'Nồi', 250000, 'lauthai.jpg', 'L02', N'Còn món'),
('M06', N'Bò bít tết', N'Phần', 150000, 'bobittet.jpg', 'L02', N'Còn món'),
('M07', N'Gà nướng mật ong', N'Con', 200000, 'ganuongmatong.jpg', 'L02', N'Còn món'),
('M08', N'Hàu nướng', N'Phần', 180000, 'haunuong.jpg', 'L02', N'Còn món'),
('M09', N'Tiger Beer', N'Lon', 20000, 'tiger.jpg', 'L03', N'Còn món'),
('M10', N'Coca Cola', N'Lon', 15000, 'cocacola.jpg', 'L03', N'Còn món'),
('M11', N'Trà đào cam sả', N'Ly', 45000, 'tradao.jpg', 'L03', N'Còn món'),
('M12', N'Nước cam ép', N'Ly', 35000, 'nuoccam.jpg', 'L03', N'Còn món'),
('M13', N'Trái cây thập cẩm', N'Dĩa', 60000, 'traicay.jpg', 'L04', N'Còn món'),
('M14', N'Kem tươi', N'Ly', 40000, 'kem.jpg', 'L04', N'Còn món'),
('M15', N'Bánh flan', N'Phần', 30000, 'flan.jpg', 'L04', N'Còn món')

-- 6. Khách hàng (Test đa dạng hạng VIP)
INSERT INTO KhachHang (SoDienThoai, TenKhach, Email, NgaySinh, DiemTichLuy, TongChiTieu) VALUES 
('0909123456', N'Khách Hạng Kim Cương', 'kimcuong@email.com', '1985-05-15', 1200, 12000000),
('0912345678', N'Khách Hạng Vàng', 'vang@email.com', '1990-08-20', 650, 6500000),
('0923456789', N'Khách Hạng Bạc', 'bac@email.com', '1995-03-10', 300, 3000000),
('0934567890', N'Khách Hạng Đồng', NULL, '2000-12-25', 50, 500000),
('0945678901', N'Nguyễn Văn Test', 'test@email.com', NULL, 0, 0)

-- Update trigger sẽ tự động set hạng VIP
UPDATE KhachHang SET DiemTichLuy = DiemTichLuy WHERE SoDienThoai IS NOT NULL

-- 7. Khuyến mãi (Data test)
INSERT INTO KhuyenMai VALUES 
('KM001', N'Giảm 20% cho hóa đơn trên 500k', N'Giảm %', 20, 500000, 
 '2026-01-01', '2026-12-31', N'Đang hoạt động', NULL),
('KM002', N'Giảm 100k cho VIP Vàng', N'Giảm tiền', 100000, 300000,
 '2026-01-01', '2026-12-31', N'Đang hoạt động', N'Vàng'),
('KM003', N'Giảm 200k cho VIP Kim cương', N'Giảm tiền', 200000, 0,
 '2026-01-01', '2026-12-31', N'Đang hoạt động', N'Kim cương'),
('KM_SINHNHAT', N'Tặng món tráng miệng sinh nhật', N'Tặng món', 0, 0,
 '2026-01-01', '2026-12-31', N'Đang hoạt động', NULL)

-- 8. Đặt bàn mẫu (Test khung giờ)
-- Đặt bàn cho hôm nay
DECLARE @HomNay DATETIME = CAST(CAST(GETDATE() AS DATE) AS DATETIME)

-- 8. Đặt bàn mẫu (Test khung giờ)
-- (Đã xóa theo yêu cầu - Data sẽ được tạo từ ứng dụng)


GO

/* ==================== TEST STORED PROCEDURES ==================== */

PRINT N'========================================='
PRINT N'TEST 1: Kiểm tra xung đột đặt bàn'
PRINT N'========================================='
DECLARE @HomNay DATETIME = CAST(CAST(GETDATE() AS DATE) AS DATETIME)
EXEC SP_KiemTraDatBan 'B03', DATEADD(HOUR, 13, @HomNay), DATEADD(HOUR, 15, @HomNay)
PRINT ''

PRINT N'========================================='
PRINT N'TEST 2: Gợi ý bàn cho 6 người buổi tối'
PRINT N'========================================='
EXEC SP_GoiYBan 6, DATEADD(HOUR, 19, @HomNay), DATEADD(HOUR, 21, @HomNay), NULL
PRINT''

PRINT N'========================================='
PRINT N'TEST 3: Kiểm tra hạng VIP tự động'
PRINT N'========================================='
SELECT SoDienThoai, TenKhach, DiemTichLuy, HangVIP FROM KhachHang
PRINT ''

PRINT N'========================================='
PRINT N'TEST 4: Tính giảm giá VIP'
PRINT N'========================================='
DECLARE @GiamGia FLOAT
EXEC SP_TinhGiamGiaVIP '0909123456', 1000000, @GiamGia OUTPUT
PRINT N'Khách VIP ' + N'Kim cương' + N' được giảm: ' + CAST(@GiamGia AS VARCHAR) + N'đ'
PRINT ''

PRINT N'========================================='
PRINT N'=== HOÀN TẤT TẠO DATABASE NÂNG CẤP ==='
PRINT N'========================================='
PRINT N'✓ Đã tạo 12 bảng với đầy đủ tính năng'
PRINT N'✓ Đã tạo 3 Triggers tự động'
PRINT N'✓ Đã tạo 6 Stored Procedures'
PRINT N'✓ Đã tạo 8 Indexes tối ưu'
PRINT N'✓ Đã tạo 2 Views thống kê'
PRINT N'✓ Đã thêm dữ liệu mẫu để test'
PRINT N''
PRINT N'Tính năng mới:'
PRINT N'  - Đặt bàn theo khung giờ (bắt đầu/kết thúc)'
PRINT N'  - Kiểm tra xung đột thời gian tự động'
PRINT N'  - Hệ thống VIP 4 hạng (tự động cập nhật)'
PRINT N'  - Quản lý khuyến mãi và voucher'
PRINT N'  - Lịch sử sử dụng bàn'
PRINT N'  - Gợi ý bàn thông minh'
PRINT N'  - Tích điểm tự động'
PRINT N''
PRINT N'Sẵn sàng sử dụng!'
GO
