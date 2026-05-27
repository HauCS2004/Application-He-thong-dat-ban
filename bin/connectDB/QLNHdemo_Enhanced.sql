/* ===================================================
 * DATABASE QUẢN LÝ NHÀ HÀNG - PHIÊN BẢN 3.0
 * Cập nhật: 2026-03-13
 *
 * Thay đổi so với v2.0:
 *   [GĐ1] Tách TaiKhoan ra khỏi NhanVien
 *   [GĐ1] Hỗ trợ Gộp Bàn / Chuyển Bàn (logic giữ nguyên, UI mới)
 *   [GĐ2] ChiTietDatBan — đặt nhiều bàn trong 1 lần đặt
 *   [GĐ3] BangGia — quản lý giá theo thời gian / khung giờ
 *   [GĐ4] HoaDon — thêm VAT, phí phục vụ, giảm giá, thành tiền
 * =================================================== */

USE master
GO

-- 1. XÓA DB CŨ NẾU CÓ
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

-- 1. Bảng Nhân Viên (chỉ lưu thông tin nhân sự, KHÔNG có mật khẩu)
CREATE TABLE NhanVien (
    MaNV         NVARCHAR(20) PRIMARY KEY,
    TenNV        NVARCHAR(50)  NOT NULL,
    SoDienThoai  VARCHAR(20)   NULL,
    Email        NVARCHAR(100) NULL,
    NgayVaoLam   DATE          DEFAULT GETDATE()
)
GO

-- 2. [MỚI GĐ1] Bảng Tài Khoản (tách khỏi NhanVien)
CREATE TABLE TaiKhoan (
    MaTK    NVARCHAR(20) PRIMARY KEY,         -- = MaNV
    MatKhau NVARCHAR(100) NOT NULL,
    VaiTro  NVARCHAR(20) DEFAULT N'Nhân viên', -- Nhân viên, Quản lý
    FOREIGN KEY (MaTK) REFERENCES NhanVien(MaNV) ON DELETE CASCADE
)
GO

-- 3. Bảng Khu Vực
CREATE TABLE KhuVuc (
    MaKV  VARCHAR(10)   PRIMARY KEY,
    TenKV NVARCHAR(50)  NOT NULL,
    MoTa  NVARCHAR(200) NULL
)
GO

-- 4. Bảng Bàn (hỗ trợ Ghép Bàn qua MaBanGop)
CREATE TABLE Ban (
    MaBan           VARCHAR(10)  PRIMARY KEY,
    TenBan          NVARCHAR(50) NOT NULL,
    TrangThai       NVARCHAR(20) DEFAULT N'Trống', -- Trống, Có Khách, Đã Đặt, Đang Gộp
    MaKV            VARCHAR(10)  NOT NULL,
    SoGhe           INT          DEFAULT 4,
    MaBanGop        VARCHAR(10)  NULL,              -- Bàn chính khi đang gộp
    ThoiGianCapNhat DATETIME     DEFAULT GETDATE(),

    FOREIGN KEY (MaKV)    REFERENCES KhuVuc(MaKV),
    FOREIGN KEY (MaBanGop) REFERENCES Ban(MaBan)
)
GO

-- 5. Bảng Loại Món
CREATE TABLE LoaiMon (
    MaLoai VARCHAR(10)   PRIMARY KEY,
    TenLoai NVARCHAR(50) NOT NULL,
    MoTa   NVARCHAR(200) NULL
)
GO

-- 6. Bảng Món Ăn (giữ DonGia làm giá mặc định / fallback)
CREATE TABLE MonAn (
    MaMon      VARCHAR(10)   PRIMARY KEY,
    TenMon     NVARCHAR(100) NOT NULL,
    DonViTinh  NVARCHAR(20),
    DonGia     FLOAT         DEFAULT 0,          -- Giá mặc định (fallback khi BangGia không có)
    HinhAnh    NVARCHAR(200) DEFAULT 'default.png',
    MaLoai     VARCHAR(10)   NOT NULL,
    TrangThai  NVARCHAR(20)  DEFAULT N'Còn món', -- Còn món, Hết món, Ngừng phục vụ
    FOREIGN KEY (MaLoai) REFERENCES LoaiMon(MaLoai)
)
GO

-- 7. [MỚI GĐ3] Bảng Giá theo Thời Gian
CREATE TABLE BangGia (
    MaGia       INT IDENTITY(1,1) PRIMARY KEY,
    MaMon       VARCHAR(10)   NOT NULL,
    DonGia      FLOAT         NOT NULL,
    TuNgay      DATE          NULL,        -- NULL = áp dụng mọi ngày
    DenNgay     DATE          NULL,
    GioBatDau   TIME          NULL,        -- NULL = cả ngày (VD: Happy Hour 17:00)
    GioKetThuc  TIME          NULL,        -- VD: 20:00
    UuTien      INT           DEFAULT 0,   -- Số cao hơn = ưu tiên hơn (override giá thấp hơn)
    GhiChu      NVARCHAR(200) NULL,
    FOREIGN KEY (MaMon) REFERENCES MonAn(MaMon) ON DELETE CASCADE
)
GO

-- 8. Bảng Khách Hàng (VIP 4 hạng)
CREATE TABLE KhachHang (
    SoDienThoai    VARCHAR(20)   PRIMARY KEY,
    TenKhach       NVARCHAR(50)  NOT NULL,
    Email          NVARCHAR(100) NULL,
    NgaySinh       DATE          NULL,
    DiemTichLuy    INT           DEFAULT 0,
    HangVIP        NVARCHAR(20)  DEFAULT N'Đồng', -- Đồng, Bạc, Vàng, Kim cương
    NgayTao        DATETIME      DEFAULT GETDATE(),
    LanGiaoDichCuoi DATETIME     NULL,
    TongChiTieu    FLOAT         DEFAULT 0,
    GhiChu         NVARCHAR(200) NULL
)
GO

-- 9. [CẬP NHẬT GĐ2] Bảng Đặt Bàn (bỏ MaBan — chuyển sang ChiTietDatBan)
CREATE TABLE DatBan (
    MaDat          INT IDENTITY(1,1) PRIMARY KEY,
    TenKhachDat    NVARCHAR(50),
    SDT            VARCHAR(20),
    ThoiGianBatDau DATETIME     NOT NULL,
    ThoiGianKetThuc DATETIME    NOT NULL,
    SoLuongKhach   INT          DEFAULT 1,
    TrangThai      NVARCHAR(20) DEFAULT N'Chờ xác nhận',
    TienCoc        FLOAT        DEFAULT 0,
    GhiChu         NVARCHAR(200),
    NgayTao        DATETIME     DEFAULT GETDATE(),
    MaHD           INT          NULL,    -- Link với hóa đơn khi khách đến

    CONSTRAINT CK_ThoiGian CHECK (ThoiGianKetThuc > ThoiGianBatDau)
)
GO

-- 10. [MỚI GĐ2] Bảng Chi Tiết Đặt Bàn (1 lần đặt — nhiều bàn)
CREATE TABLE ChiTietDatBan (
    MaDat  INT         NOT NULL,
    MaBan  VARCHAR(10) NOT NULL,
    PRIMARY KEY (MaDat, MaBan),
    FOREIGN KEY (MaDat) REFERENCES DatBan(MaDat) ON DELETE CASCADE,
    FOREIGN KEY (MaBan) REFERENCES Ban(MaBan)
)
GO

-- 11. [CẬP NHẬT GĐ4] Bảng Hóa Đơn (thêm VAT, phí phục vụ, giảm giá)
CREATE TABLE HoaDon (
    MaHD        INT IDENTITY(1,1) PRIMARY KEY,
    NgayTao     DATETIME    DEFAULT GETDATE(),
    TongTien    FLOAT       DEFAULT 0,              -- Tổng tiền món ăn (chưa VAT)
    PhanTramVAT FLOAT       DEFAULT 10,             -- % VAT (VD: 10%)
    PhiPhucVu   FLOAT       DEFAULT 5,              -- % phí phục vụ (VD: 5%)
    TienGiamGia FLOAT       DEFAULT 0,              -- Tiền giảm (từ KM + VIP)
    ThanhTien   FLOAT       DEFAULT 0,              -- Tổng cuối = TongTien*(1+VAT%+Phi%) - Giam
    TrangThai   INT         DEFAULT 0,              -- 0: Chưa thanh toán, 1: Đã thanh toán
    MaBan       VARCHAR(10) NOT NULL,               -- Bàn chính (khi gộp bàn: bàn đích)
    SoLuongKhach INT        DEFAULT 1,
    SDT_Khach   VARCHAR(20) NULL,
    GhiChu      NVARCHAR(100) NULL,
    MaNV        NVARCHAR(20) NULL,

    FOREIGN KEY (MaBan)     REFERENCES Ban(MaBan),
    FOREIGN KEY (SDT_Khach) REFERENCES KhachHang(SoDienThoai),
    FOREIGN KEY (MaNV)      REFERENCES NhanVien(MaNV)
)
GO

-- 12. Bảng Chi Tiết Hóa Đơn
CREATE TABLE ChiTietHoaDon (
    MaHD     INT         NOT NULL,
    MaMon    VARCHAR(10) NOT NULL,
    SoLuong  INT         DEFAULT 1,
    DonGia   FLOAT,                              -- Giá tại thời điểm order (snapshot từ BangGia)
    GhiChu   NVARCHAR(100) NULL,

    PRIMARY KEY (MaHD, MaMon),
    FOREIGN KEY (MaHD)  REFERENCES HoaDon(MaHD) ON DELETE CASCADE,
    FOREIGN KEY (MaMon) REFERENCES MonAn(MaMon)
)
GO

-- 13. Bảng Khuyến Mãi
CREATE TABLE KhuyenMai (
    MaKM              VARCHAR(20) PRIMARY KEY,
    TenKM             NVARCHAR(100) NOT NULL,
    LoaiKM            NVARCHAR(20)  CHECK (LoaiKM IN (N'Giảm %', N'Giảm tiền', N'Tặng món')),
    GiaTri            FLOAT,
    DieuKienToiThieu  FLOAT         DEFAULT 0,
    NgayBatDau        DATETIME,
    NgayKetThuc       DATETIME,
    TrangThai         NVARCHAR(20)  DEFAULT N'Đang hoạt động',
    HangVIPApDung     NVARCHAR(20)  NULL,       -- NULL = tất cả hạng

    CONSTRAINT CK_KM_ThoiGian CHECK (NgayKetThuc > NgayBatDau)
)
GO

-- 14. Bảng Liên kết: Hóa đơn - Khuyến mãi
CREATE TABLE HoaDon_KhuyenMai (
    MaHD    INT         NOT NULL,
    MaKM    VARCHAR(20) NOT NULL,
    GiamGia FLOAT,

    PRIMARY KEY (MaHD, MaKM),
    FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD),
    FOREIGN KEY (MaKM) REFERENCES KhuyenMai(MaKM)
)
GO

-- 15. Bảng Lịch Sử Bàn
CREATE TABLE LichSuBan (
    MaLichSu       INT IDENTITY(1,1) PRIMARY KEY,
    MaBan          VARCHAR(10) NOT NULL,
    ThoiGianBatDau DATETIME    NOT NULL,
    ThoiGianKetThuc DATETIME   NULL,
    SoLuongKhach   INT,
    DoanhThu       FLOAT       DEFAULT 0,
    MaHD           INT         NULL,

    FOREIGN KEY (MaBan) REFERENCES Ban(MaBan),
    FOREIGN KEY (MaHD)  REFERENCES HoaDon(MaHD)
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
            WHEN DiemTichLuy >= 500  THEN N'Vàng'
            WHEN DiemTichLuy >= 200  THEN N'Bạc'
            ELSE N'Đồng'
        END
        WHERE SoDienThoai IN (SELECT SoDienThoai FROM inserted)
    END
END
GO

-- Trigger 2: [CẬP NHẬT GĐ2] Link DatBan -> HoaDon qua ChiTietDatBan
CREATE TRIGGER TR_LinkDatBanHoaDon
ON HoaDon
AFTER INSERT
AS
BEGIN
    -- Link booking với hóa đơn: tìm đặt bàn cho bàn chính của HD đang trong giờ
    UPDATE DatBan
    SET MaHD = i.MaHD,
        TrangThai = N'Hoàn thành'
    FROM DatBan db
    INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat
    INNER JOIN inserted i ON ctdb.MaBan = i.MaBan
    WHERE db.TrangThai = N'Đã nhận bàn'
      AND GETDATE() BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc
END
GO

-- Trigger 3: [CẬP NHẬT GĐ4] Tính TongTien khi thêm/sửa/xóa chi tiết
-- (chỉ tính TongTien = tổng món, ThanhTien được tính khi thanh toán)
CREATE TRIGGER TR_TinhTongTien
ON ChiTietHoaDon
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
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

-- SP 1: [CẬP NHẬT GĐ2] Kiểm tra xung đột đặt bàn (qua ChiTietDatBan)
CREATE PROCEDURE SP_KiemTraDatBan
    @MaBan           VARCHAR(10),
    @ThoiGianBatDau  DATETIME,
    @ThoiGianKetThuc DATETIME,
    @MaDatHienTai    INT = NULL
AS
BEGIN
    SELECT COUNT(*) AS SoLuongXungDot
    FROM DatBan db
    INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat
    WHERE ctdb.MaBan = @MaBan
      AND (@MaDatHienTai IS NULL OR db.MaDat != @MaDatHienTai)
      AND db.TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã nhận bàn')
      AND (
          (@ThoiGianBatDau  BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc)
          OR (@ThoiGianKetThuc BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc)
          OR (db.ThoiGianBatDau  BETWEEN @ThoiGianBatDau AND @ThoiGianKetThuc)
      )
END
GO

-- SP 2: [CẬP NHẬT GĐ2] Gợi ý bàn phù hợp
CREATE PROCEDURE SP_GoiYBan
    @SoKhach         INT,
    @ThoiGianBatDau  DATETIME,
    @ThoiGianKetThuc DATETIME,
    @MaKV            VARCHAR(10) = NULL
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
      AND b.TrangThai NOT IN (N'Đang Gộp')
      AND b.MaBan NOT IN (
          -- Loại bỏ bàn bị đặt trong khung giờ này
          SELECT ctdb.MaBan
          FROM DatBan db
          INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat
          WHERE db.TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã nhận bàn')
            AND (
                (@ThoiGianBatDau  BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc)
                OR (@ThoiGianKetThuc BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc)
                OR (db.ThoiGianBatDau  BETWEEN @ThoiGianBatDau AND @ThoiGianKetThuc)
            )
      )
    ORDER BY ChenhLech, b.SoGhe
END
GO

-- SP 3: Tích điểm cho khách hàng
CREATE PROCEDURE SP_TichDiem
    @MaHD INT,
    @SDT  VARCHAR(20)
AS
BEGIN
    DECLARE @TongTien FLOAT
    SELECT @TongTien = ThanhTien FROM HoaDon WHERE MaHD = @MaHD

    -- Quy tắc: Mỗi 10,000đ = 1 điểm (tính trên ThanhTien)
    DECLARE @DiemThem INT = FLOOR(@TongTien / 10000)

    IF EXISTS (SELECT 1 FROM KhachHang WHERE SoDienThoai = @SDT)
    BEGIN
        UPDATE KhachHang
        SET DiemTichLuy    = DiemTichLuy + @DiemThem,
            LanGiaoDichCuoi = GETDATE(),
            TongChiTieu    = TongChiTieu + @TongTien
        WHERE SoDienThoai = @SDT

        PRINT N'Đã tích ' + CAST(@DiemThem AS VARCHAR) + N' điểm cho khách hàng ' + @SDT
    END
    ELSE
        PRINT N'Không tìm thấy khách hàng với SĐT: ' + @SDT
END
GO

-- SP 4: Tính giảm giá VIP
CREATE PROCEDURE SP_TinhGiamGiaVIP
    @SDT      VARCHAR(20),
    @TongTien FLOAT,
    @GiamGia  FLOAT OUTPUT
AS
BEGIN
    DECLARE @HangVIP NVARCHAR(20)
    SELECT @HangVIP = HangVIP FROM KhachHang WHERE SoDienThoai = @SDT

    SET @GiamGia = CASE
        WHEN @HangVIP = N'Kim cương' THEN @TongTien * 0.15
        WHEN @HangVIP = N'Vàng'      THEN @TongTien * 0.10
        WHEN @HangVIP = N'Bạc'       THEN @TongTien * 0.05
        ELSE 0
    END
END
GO

-- SP 5: [CẬP NHẬT GĐ2] Đặt bàn nhiều bàn
CREATE PROCEDURE SP_DatBan
    @TenKhachDat     NVARCHAR(50),
    @SDT             VARCHAR(20),
    @ThoiGianBatDau  DATETIME,
    @ThoiGianKetThuc DATETIME,
    @SoLuongKhach    INT,
    @DanhSachBan     NVARCHAR(MAX), -- Danh sách mã bàn phân cách bởi ',' VD: 'B01,B02'
    @GhiChu          NVARCHAR(200) = NULL,
    @TienCoc         FLOAT = 0,
    @KetQua          INT OUTPUT,
    @ThongBao        NVARCHAR(200) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra xung đột cho từng bàn trong danh sách
    DECLARE @BanList TABLE (MaBan VARCHAR(10))
    INSERT INTO @BanList
    SELECT value FROM STRING_SPLIT(@DanhSachBan, ',')

    DECLARE @XungDot INT = 0
    SELECT @XungDot = COUNT(*)
    FROM @BanList bl
    WHERE EXISTS (
        SELECT 1
        FROM DatBan db
        INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat
        WHERE ctdb.MaBan = bl.MaBan
          AND db.TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã nhận bàn')
          AND (
              (@ThoiGianBatDau  BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc)
              OR (@ThoiGianKetThuc BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc)
              OR (db.ThoiGianBatDau  BETWEEN @ThoiGianBatDau AND @ThoiGianKetThuc)
          )
    )

    IF @XungDot > 0
    BEGIN
        SET @KetQua = 0
        SET @ThongBao = N'Một hoặc nhiều bàn đã được đặt trong khung giờ này!'
        RETURN
    END

    -- Tạo booking
    INSERT INTO DatBan (TenKhachDat, SDT, ThoiGianBatDau, ThoiGianKetThuc,
                        SoLuongKhach, GhiChu, TienCoc, TrangThai)
    VALUES (@TenKhachDat, @SDT, @ThoiGianBatDau, @ThoiGianKetThuc,
            @SoLuongKhach, @GhiChu, @TienCoc, N'Chờ xác nhận')

    DECLARE @MaDatMoi INT = SCOPE_IDENTITY()

    -- Thêm từng bàn vào ChiTietDatBan
    INSERT INTO ChiTietDatBan (MaDat, MaBan)
    SELECT @MaDatMoi, MaBan FROM @BanList

    SET @KetQua = 1
    SET @ThongBao = N'Đặt bàn thành công! Mã đặt: ' + CAST(@MaDatMoi AS VARCHAR)
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
        COUNT(MaHD)           AS SoHoaDon,
        SUM(SoLuongKhach)     AS TongKhach,
        SUM(ThanhTien)        AS DoanhThu   -- Dùng ThanhTien (sau VAT/phí/giảm)
    FROM HoaDon
    WHERE TrangThai = 1
      AND CAST(NgayTao AS DATE) BETWEEN @TuNgay AND @DenNgay
    GROUP BY CAST(NgayTao AS DATE)
    ORDER BY Ngay DESC
END
GO

-- SP 7: [MỚI GĐ3] Lấy giá hiện tại của món ăn
CREATE PROCEDURE SP_LayGiaHienTai
    @MaMon    VARCHAR(10),
    @ThoiDiem DATETIME = NULL   -- NULL = GETDATE()
AS
BEGIN
    SET NOCOUNT ON;
    IF @ThoiDiem IS NULL SET @ThoiDiem = GETDATE()

    DECLARE @GiaKetQua FLOAT

    -- Tìm giá đặc biệt ưu tiên cao nhất phù hợp thời điểm
    SELECT TOP 1 @GiaKetQua = DonGia
    FROM BangGia
    WHERE MaMon = @MaMon
      AND (TuNgay  IS NULL OR CAST(@ThoiDiem AS DATE) >= TuNgay)
      AND (DenNgay IS NULL OR CAST(@ThoiDiem AS DATE) <= DenNgay)
      AND (GioBatDau  IS NULL OR CAST(@ThoiDiem AS TIME) >= GioBatDau)
      AND (GioKetThuc IS NULL OR CAST(@ThoiDiem AS TIME) <= GioKetThuc)
    ORDER BY UuTien DESC

    -- Nếu không có giá đặc biệt → dùng giá gốc trong MonAn
    IF @GiaKetQua IS NULL
        SELECT @GiaKetQua = DonGia FROM MonAn WHERE MaMon = @MaMon

    SELECT ISNULL(@GiaKetQua, 0) AS DonGia
END
GO

-- SP 8: [MỚI GĐ4] Thanh toán hóa đơn (tính đủ VAT, phí, giảm giá)
CREATE PROCEDURE SP_ThanhToan
    @MaHD       INT,
    @MaKM       VARCHAR(20) = NULL,    -- Mã khuyến mãi (nếu có)
    @SDT        VARCHAR(20) = NULL,    -- SDT khách (để áp giảm VIP)
    @VAT        FLOAT = 10,            -- % VAT
    @PhiPhucVu  FLOAT = 5             -- % phí phục vụ
AS
BEGIN
    SET NOCOUNT ON;

    -- 1. Lấy tổng tiền món
    DECLARE @TongTienMon FLOAT
    SELECT @TongTienMon = ISNULL(SUM(SoLuong * DonGia), 0)
    FROM ChiTietHoaDon
    WHERE MaHD = @MaHD

    -- 2. Tính giảm giá VIP
    DECLARE @GiamVIP FLOAT = 0
    IF @SDT IS NOT NULL
        EXEC SP_TinhGiamGiaVIP @SDT, @TongTienMon, @GiamVIP OUTPUT

    -- 3. Tính giảm giá khuyến mãi
    DECLARE @GiamKM FLOAT = 0
    IF @MaKM IS NOT NULL
    BEGIN
        DECLARE @LoaiKM NVARCHAR(20), @GiaTriKM FLOAT, @DieuKien FLOAT
        SELECT @LoaiKM = LoaiKM, @GiaTriKM = GiaTri, @DieuKien = DieuKienToiThieu
        FROM KhuyenMai
        WHERE MaKM = @MaKM AND TrangThai = N'Đang hoạt động'
          AND GETDATE() BETWEEN NgayBatDau AND NgayKetThuc

        IF @GiaTriKM IS NOT NULL AND @TongTienMon >= @DieuKien
        BEGIN
            IF @LoaiKM = N'Giảm %'
                SET @GiamKM = @TongTienMon * @GiaTriKM / 100
            ELSE IF @LoaiKM = N'Giảm tiền'
                SET @GiamKM = @GiaTriKM
        END
    END

    DECLARE @TongGiam FLOAT = @GiamVIP + @GiamKM

    -- 4. Tính thành tiền cuối
    DECLARE @ThanhTien FLOAT
    SET @ThanhTien = @TongTienMon * (1 + @VAT / 100 + @PhiPhucVu / 100) - @TongGiam
    IF @ThanhTien < 0 SET @ThanhTien = 0

    -- 5. Cập nhật hóa đơn
    UPDATE HoaDon
    SET TongTien    = @TongTienMon,
        PhanTramVAT = @VAT,
        PhiPhucVu   = @PhiPhucVu,
        TienGiamGia = @TongGiam,
        ThanhTien   = @ThanhTien,
        TrangThai   = 1,
        SDT_Khach   = ISNULL(@SDT, SDT_Khach)
    WHERE MaHD = @MaHD

    -- 6. Ghi nhận khuyến mãi đã dùng
    IF @MaKM IS NOT NULL AND @GiamKM > 0
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM HoaDon_KhuyenMai WHERE MaHD = @MaHD AND MaKM = @MaKM)
            INSERT INTO HoaDon_KhuyenMai (MaHD, MaKM, GiamGia)
            VALUES (@MaHD, @MaKM, @GiamKM)
    END

    -- 7. Tích điểm
    IF @SDT IS NOT NULL
        EXEC SP_TichDiem @MaHD, @SDT

    SELECT @ThanhTien AS ThanhTien, @TongGiam AS TongGiam, @TongTienMon AS TongTienMon
END
GO


/* ==================== INDEXES ==================== */

-- Đặt bàn
CREATE INDEX IDX_DatBan_ThoiGian    ON DatBan(ThoiGianBatDau, ThoiGianKetThuc, TrangThai)
CREATE INDEX IDX_ChiTietDatBan_Ban  ON ChiTietDatBan(MaBan)
CREATE INDEX IDX_ChiTietDatBan_Dat  ON ChiTietDatBan(MaDat)

-- Khách hàng
CREATE INDEX IDX_KhachHang_Diem ON KhachHang(DiemTichLuy DESC)
CREATE INDEX IDX_KhachHang_Hang ON KhachHang(HangVIP)

-- Hóa đơn
CREATE INDEX IDX_HoaDon_NgayTao  ON HoaDon(NgayTao DESC)
CREATE INDEX IDX_HoaDon_TrangThai ON HoaDon(TrangThai)
CREATE INDEX IDX_HoaDon_MaBan    ON HoaDon(MaBan, TrangThai)

-- Món ăn
CREATE INDEX IDX_MonAn_Loai ON MonAn(MaLoai, TrangThai)

-- Bảng giá
CREATE INDEX IDX_BangGia_Mon ON BangGia(MaMon, UuTien DESC)

-- Bàn
CREATE INDEX IDX_Ban_KhuVuc ON Ban(MaKV, TrangThai)
GO


/* ==================== VIEWS ==================== */

-- View 1: [CẬP NHẬT GĐ2] Trạng thái bàn (join qua ChiTietDatBan)
CREATE VIEW V_TrangThaiBanChiTiet AS
SELECT
    b.MaBan,
    b.TenBan,
    b.TrangThai     AS TrangThaiHienTai,
    b.SoGhe,
    kv.TenKV,
    db.MaDat,
    db.ThoiGianBatDau,
    db.ThoiGianKetThuc,
    db.TenKhachDat,
    db.SDT,
    db.TrangThai    AS TrangThaiDatBan,
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
LEFT JOIN ChiTietDatBan ctdb ON b.MaBan = ctdb.MaBan
LEFT JOIN DatBan db ON ctdb.MaDat = db.MaDat
    AND db.TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã nhận bàn')
    AND GETDATE() BETWEEN DATEADD(HOUR, -1, db.ThoiGianBatDau) AND db.ThoiGianKetThuc
GO

-- View 2: Thống kê khách hàng VIP
CREATE VIEW V_ThongKeKhachVIP AS
SELECT
    HangVIP,
    COUNT(*)           AS SoLuong,
    SUM(TongChiTieu)   AS TongDoanhThu,
    AVG(TongChiTieu)   AS TrungBinhChiTieu,
    AVG(DiemTichLuy)   AS TrungBinhDiem
FROM KhachHang
GROUP BY HangVIP
GO


/* ==================== DỮ LIỆU MẪU ==================== */

-- 1. Nhân viên
INSERT INTO NhanVien (MaNV, TenNV, SoDienThoai) VALUES
('admin', N'Quản Lý Hệ Thống', '0901234567'),
('nv01',  N'Nguyễn Văn A',     '0912345678'),
('nv02',  N'Trần Thị B',       '0923456789')
GO

-- 2. Tài khoản
INSERT INTO TaiKhoan (MaTK, MatKhau, VaiTro) VALUES
('admin', '123', N'Quản lý'),
('nv01',  '123', N'Nhân viên'),
('nv02',  '123', N'Nhân viên')
GO

-- 3. Khu vực
INSERT INTO KhuVuc VALUES
('KV01', N'Tầng 1',    N'Khu vực khách đơn và đôi'),
('KV02', N'Tầng 2',    N'Khu vực gia đình'),
('KV03', N'VIP',       N'Phòng VIP và tiệc lớn')
GO

-- 4. Bàn
INSERT INTO Ban (MaBan, TenBan, TrangThai, MaKV, SoGhe) VALUES
('B01',   N'Bàn 1',        N'Trống', 'KV01', 2),
('B02',   N'Bàn 2',        N'Trống', 'KV01', 2),
('B03',   N'Bàn 3',        N'Trống', 'KV01', 4),
('B04',   N'Bàn 4',        N'Trống', 'KV01', 4),
('B05',   N'Bàn 5',        N'Trống', 'KV01', 4),
('B06',   N'Bàn 6',        N'Trống', 'KV02', 6),
('B07',   N'Bàn 7',        N'Trống', 'KV02', 6),
('B08',   N'Bàn 8',        N'Trống', 'KV02', 8),
('B09',   N'Bàn 9',        N'Trống', 'KV02', 8),
('VIP01', N'Phòng VIP 1',  N'Trống', 'KV03', 12),
('VIP02', N'Phòng VIP 2',  N'Trống', 'KV03', 20)
GO

-- 5. Loại món
INSERT INTO LoaiMon VALUES
('L01', N'Khai vị',    N'Món mở đầu bữa ăn'),
('L02', N'Món chính',  N'Món chính của bữa ăn'),
('L03', N'Đồ uống',    N'Nước giải khát'),
('L04', N'Tráng miệng',N'Món ăn kết thúc bữa ăn')
GO

-- 6. Món ăn (DonGia = giá mặc định / fallback)
INSERT INTO MonAn (MaMon, TenMon, DonViTinh, DonGia, HinhAnh, MaLoai, TrangThai) VALUES
('M01', N'Khoai tây chiên',      N'Dĩa',  45000, 'khoaitaychien.jpg', 'L01', N'Còn món'),
('M02', N'Gỏi ngó sen tôm thịt', N'Dĩa',  85000, 'ngosentomthit.jpg', 'L01', N'Còn món'),
('M03', N'Salad trộn',           N'Dĩa',  55000, 'salad.jpg',         'L01', N'Còn món'),
('M04', N'Cơm chiên hải sản',    N'Dĩa', 120000, 'comchienhs.jpg',    'L02', N'Còn món'),
('M05', N'Lẩu Thái Lan',         N'Nồi', 250000, 'lauthai.jpg',       'L02', N'Còn món'),
('M06', N'Bò bít tết',           N'Phần',150000, 'bobittet.jpg',      'L02', N'Còn món'),
('M07', N'Gà nướng mật ong',     N'Con', 200000, 'ganuongmatong.jpg', 'L02', N'Còn món'),
('M08', N'Hàu nướng',            N'Phần',180000, 'haunuong.jpg',      'L02', N'Còn món'),
('M09', N'Tiger Beer',           N'Lon',  20000, 'tiger.jpg',         'L03', N'Còn món'),
('M10', N'Coca Cola',            N'Lon',  15000, 'cocacola.jpg',      'L03', N'Còn món'),
('M11', N'Trà đào cam sả',       N'Ly',   45000, 'tradao.jpg',        'L03', N'Còn món'),
('M12', N'Nước cam ép',          N'Ly',   35000, 'nuoccam.jpg',       'L03', N'Còn món'),
('M13', N'Trái cây thập cẩm',    N'Dĩa',  60000, 'traicay.jpg',       'L04', N'Còn món'),
('M14', N'Kem tươi',             N'Ly',   40000, 'kem.jpg',           'L04', N'Còn món'),
('M15', N'Bánh flan',            N'Phần', 30000, 'flan.jpg',          'L04', N'Còn món')
GO

-- 7. Bảng giá mẫu (GĐ3 — demo Happy Hour Tiger Beer)
INSERT INTO BangGia (MaMon, DonGia, TuNgay, DenNgay, GioBatDau, GioKetThuc, UuTien, GhiChu) VALUES
-- Happy Hour Tiger Beer 17:00-20:00 tất cả các ngày trong năm 2026
('M09', 25000, '2026-01-01', '2026-12-31', '17:00:00', '20:00:00', 10, N'Happy Hour - giá tăng buổi tối')
GO

-- 8. Khách hàng mẫu
INSERT INTO KhachHang (SoDienThoai, TenKhach, Email, NgaySinh, DiemTichLuy, TongChiTieu) VALUES
('0909123456', N'Khách VIP Kim Cương', 'kimcuong@email.com', '1985-05-15', 1200, 12000000),
('0912345678', N'Khách VIP Vàng',      'vang@email.com',     '1990-08-20',  650,  6500000),
('0923456789', N'Khách VIP Bạc',       'bac@email.com',      '1995-03-10',  300,  3000000),
('0934567890', N'Khách VIP Đồng',      NULL,                 '2000-12-25',   50,   500000),
('0945678901', N'Nguyễn Văn Test',     'test@email.com',     NULL,            0,         0)
GO

-- Kích hoạt trigger VIP
UPDATE KhachHang SET DiemTichLuy = DiemTichLuy WHERE SoDienThoai IS NOT NULL
GO

-- 9. Khuyến mãi mẫu
INSERT INTO KhuyenMai VALUES
('KM001', N'Giảm 20% cho hóa đơn trên 500k', N'Giảm %',    20,     500000, '2026-01-01', '2026-12-31', N'Đang hoạt động', NULL),
('KM002', N'Giảm 100k cho VIP Vàng',          N'Giảm tiền', 100000, 300000, '2026-01-01', '2026-12-31', N'Đang hoạt động', N'Vàng'),
('KM003', N'Giảm 200k cho VIP Kim cương',     N'Giảm tiền', 200000, 0,      '2026-01-01', '2026-12-31', N'Đang hoạt động', N'Kim cương'),
('KM_SN', N'Tặng món tráng miệng sinh nhật',  N'Tặng món',  0,      0,      '2026-01-01', '2026-12-31', N'Đang hoạt động', NULL)
GO


/* ==================== KIỂM TRA ==================== */

PRINT N'======================================='
PRINT N'TEST 1: Kiểm tra xung đột đặt bàn'
PRINT N'======================================='
DECLARE @HomNay DATETIME = CAST(CAST(GETDATE() AS DATE) AS DATETIME)
EXEC SP_KiemTraDatBan 'B03', DATEADD(HOUR, 13, @HomNay), DATEADD(HOUR, 15, @HomNay)

PRINT N'======================================='
PRINT N'TEST 2: Gợi ý bàn cho 6 người buổi tối'
PRINT N'======================================='
EXEC SP_GoiYBan 6, DATEADD(HOUR, 19, @HomNay), DATEADD(HOUR, 21, @HomNay), NULL

PRINT N'======================================='
PRINT N'TEST 3: Giá Tiger Beer lúc 18:30 hôm nay'
PRINT N'======================================='
DECLARE @HappyHour DATETIME = CAST(CAST(GETDATE() AS DATE) AS DATETIME) + CAST('18:30:00' AS DATETIME)
EXEC SP_LayGiaHienTai 'M09', @HappyHour

PRINT N'======================================='
PRINT N'TEST 4: Hạng VIP khách hàng'
PRINT N'======================================='
SELECT SoDienThoai, TenKhach, DiemTichLuy, HangVIP FROM KhachHang

PRINT N''
PRINT N'=== HOÀN TẤT TẠO DATABASE v3.0 ==='
PRINT N'✓ 15 bảng (thêm TaiKhoan, ChiTietDatBan, BangGia)'
PRINT N'✓ 3 Triggers'
PRINT N'✓ 8 Stored Procedures'
PRINT N'✓ 11 Indexes'
PRINT N'✓ 2 Views'
PRINT N'✓ Dữ liệu mẫu đầy đủ'
GO
