/* ===================================================
 * DATABASE QUẢN LÝ NHÀ HÀNG - PHIÊN BẢN 3.2
 * Cập nhật: 2026-03-25
 *
 * Thay đổi so với v3.1:
 * [MỚI] NhanVien: Thêm CCCD, GioiTinh, TrangThai
 * [MỚI] HoaDon: Đổi TrangThai -> NVARCHAR, thêm PhuongThucThanhToan, ThoiGianThanhToan
 * [MỚI] ChiTietHoaDon: Thêm TrangThaiMon, ThoiGianGoi (Hỗ trợ nhà bếp)
 * [MỚI] Thêm SP_LayMenuHienTai (Hỗ trợ load menu lên UI mượt mà)
 * [MỚI] Cập nhật SP_ThanhToan và SP_ThongKeDoanhThu theo cấu trúc mới
 * =================================================== */

USE master
GO

IF EXISTS (SELECT * FROM sys.databases WHERE name = 'QuanLyNhaHang_V3')
BEGIN
    ALTER DATABASE QuanLyNhaHang_V3 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyNhaHang_V3;
END
GO

CREATE DATABASE QuanLyNhaHang_V3
GO

USE QuanLyNhaHang_V3
GO

/* ====================================================
   TẠO BẢNG
   ==================================================== */

-- 1. Nhân Viên
CREATE TABLE NhanVien (
    MaNV         NVARCHAR(20)  PRIMARY KEY,
    TenNV        NVARCHAR(50)  NOT NULL,
    GioiTinh     NVARCHAR(10)  NULL,         -- [MỚI]
    SoDienThoai  VARCHAR(20)   NULL,
    Email        NVARCHAR(100) NULL,
    CCCD         VARCHAR(20)   NULL,         -- [MỚI]
    NgayVaoLam   DATE          DEFAULT GETDATE(),
    TrangThai    NVARCHAR(20)  DEFAULT N'Đang làm việc' -- [MỚI] Đang làm việc | Đã nghỉ
)
GO

-- 2. Tài Khoản
CREATE TABLE TaiKhoan (
    MaTK    NVARCHAR(20)  PRIMARY KEY,
    MatKhau NVARCHAR(100) NOT NULL,
    VaiTro  NVARCHAR(20)  DEFAULT N'Nhân viên',
    FOREIGN KEY (MaTK) REFERENCES NhanVien(MaNV) ON DELETE CASCADE
)
GO

-- 3. Khu Vực
CREATE TABLE KhuVuc (
    MaKV  VARCHAR(10)  PRIMARY KEY,
    TenKV NVARCHAR(50) NOT NULL,
    MoTa  NVARCHAR(200) NULL
)
GO

-- 4. Bàn
CREATE TABLE Ban (
    MaBan           VARCHAR(10)  PRIMARY KEY,
    TenBan          NVARCHAR(50) NOT NULL,
    TrangThai       NVARCHAR(20) DEFAULT N'Trống',
    MaKV            VARCHAR(10)  NOT NULL,
    SoGhe           INT          DEFAULT 4,
    MaBanGop        VARCHAR(10)  NULL,
    ThoiGianCapNhat DATETIME     DEFAULT GETDATE(),

    FOREIGN KEY (MaKV)     REFERENCES KhuVuc(MaKV),
    FOREIGN KEY (MaBanGop) REFERENCES Ban(MaBan)
)
GO

-- 5. Loại Món
CREATE TABLE LoaiMon (
    MaLoai  VARCHAR(10)  PRIMARY KEY,
    TenLoai NVARCHAR(50) NOT NULL,
    MoTa    NVARCHAR(200) NULL
)
GO

-- 6. Món Ăn
CREATE TABLE MonAn (
    MaMon     VARCHAR(10)   PRIMARY KEY,
    TenMon    NVARCHAR(100) NOT NULL,
    DonViTinh NVARCHAR(20)  NULL,
    DonGia    DECIMAL(18,2) DEFAULT 0,
    HinhAnh   NVARCHAR(200) DEFAULT 'default.png',
    MaLoai    VARCHAR(10)   NOT NULL,
    TrangThai NVARCHAR(20)  DEFAULT N'Còn món',

    FOREIGN KEY (MaLoai) REFERENCES LoaiMon(MaLoai)
)
GO

-- 7. Bảng Giá — HEADER
CREATE TABLE BangGia (
    MaBG       INT IDENTITY(1,1) PRIMARY KEY,
    TenBG      NVARCHAR(100) NOT NULL,
    LoaiBG     NVARCHAR(30)  DEFAULT N'Thường',
    NgayBatDau     DATE          NULL,
    NgayKetThuc    DATE          NULL,
    GioBatDau  TIME          NULL,
    GioKetThuc TIME          NULL,
    UuTien     INT           DEFAULT 0,
    TrangThai  NVARCHAR(20)  DEFAULT N'Hoạt động',
    GhiChu     NVARCHAR(200) NULL,
    NgayTao    DATETIME      DEFAULT GETDATE(),

    CONSTRAINT CK_BG_Ngay CHECK (NgayKetThuc IS NULL OR NgayKetThuc >= NgayBatDau)
)
GO

-- 8. Chi Tiết Bảng Giá — DETAIL
CREATE TABLE ChiTietBangGia (
    MaBG   INT           NOT NULL,
    MaMon  VARCHAR(10)   NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    GhiChu NVARCHAR(100) NULL,

    PRIMARY KEY (MaBG, MaMon),
    FOREIGN KEY (MaBG)  REFERENCES BangGia(MaBG)  ON DELETE CASCADE,
    FOREIGN KEY (MaMon) REFERENCES MonAn(MaMon)
)
GO

-- 9. Khách Hàng
CREATE TABLE KhachHang (
    SoDienThoai     VARCHAR(20)   PRIMARY KEY,
    TenKhach        NVARCHAR(50)  NOT NULL,
    Email           NVARCHAR(100) NULL,
    NgaySinh        DATE          NULL,
    DiemTichLuy     INT           DEFAULT 0,
    HangVIP         NVARCHAR(20)  DEFAULT N'Đồng',
    NgayTao         DATETIME      DEFAULT GETDATE(),
    LanGiaoDichCuoi DATETIME      NULL,
    TongChiTieu     DECIMAL(18,2) DEFAULT 0,
    GhiChu          NVARCHAR(200) NULL
)
GO

-- 10. Đặt Bàn — Header
CREATE TABLE DatBan (
    MaDat           INT IDENTITY(1,1) PRIMARY KEY,
    TenKhachDat     NVARCHAR(50)  NULL,
    SDT             VARCHAR(20)   NULL,
    ThoiGianBatDau  DATETIME      NOT NULL,
    ThoiGianKetThuc DATETIME      NOT NULL,
    SoLuongKhach    INT           DEFAULT 1,
    TrangThai       NVARCHAR(20)  DEFAULT N'Chờ xác nhận',
    TienCoc         DECIMAL(18,2) DEFAULT 0,
    GhiChu          NVARCHAR(200) NULL,
    NgayTao         DATETIME      DEFAULT GETDATE(),
    MaHD            INT           NULL, 

    CONSTRAINT CK_DatBan_ThoiGian CHECK (ThoiGianKetThuc > ThoiGianBatDau)
)
GO

-- 11. Chi Tiết Đặt Bàn
CREATE TABLE ChiTietDatBan (
    MaDat INT         NOT NULL,
    MaBan VARCHAR(10) NOT NULL,

    PRIMARY KEY (MaDat, MaBan),
    FOREIGN KEY (MaDat) REFERENCES DatBan(MaDat) ON DELETE CASCADE,
    FOREIGN KEY (MaBan) REFERENCES Ban(MaBan)
)
GO

-- 12. Hóa Đơn
CREATE TABLE HoaDon (
    MaHD                INT IDENTITY(1,1) PRIMARY KEY,
    NgayTao             DATETIME      DEFAULT GETDATE(),
    TongTien            DECIMAL(18,2) DEFAULT 0, 
    PhanTramVAT         DECIMAL(5,2)  DEFAULT 10,
    PhiPhucVu           DECIMAL(5,2)  DEFAULT 5, 
    TienGiamGia         DECIMAL(18,2) DEFAULT 0, 
    ThanhTien           DECIMAL(18,2) DEFAULT 0, 
    TrangThai           NVARCHAR(20)  DEFAULT N'Chưa thanh toán', -- [MỚI] Chưa thanh toán | Đã thanh toán | Đã hủy
    PhuongThucThanhToan NVARCHAR(50)  NULL,                       -- [MỚI] Tiền mặt, Chuyển khoản, Quẹt thẻ...
    ThoiGianThanhToan   DATETIME      NULL,                       -- [MỚI] Giờ chốt bill
    MaBan               VARCHAR(10)   NOT NULL, 
    SoLuongKhach        INT           DEFAULT 1,
    SDT_Khach           VARCHAR(20)   NULL,
    GhiChu              NVARCHAR(100) NULL,
    MaNV                NVARCHAR(20)  NULL,

    FOREIGN KEY (MaBan)     REFERENCES Ban(MaBan),
    FOREIGN KEY (SDT_Khach) REFERENCES KhachHang(SoDienThoai),
    FOREIGN KEY (MaNV)      REFERENCES NhanVien(MaNV)
)
GO

-- 13. Chi Tiết Hóa Đơn
CREATE TABLE ChiTietHoaDon (
    MaHD         INT           NOT NULL,
    MaMon        VARCHAR(10)   NOT NULL,
    SoLuong      INT           DEFAULT 1,
    DonGia       DECIMAL(18,2) NOT NULL,
    TrangThaiMon NVARCHAR(20)  DEFAULT N'Chờ chế biến', -- [MỚI] Chờ chế biến | Đang nấu | Đã phục vụ | Hủy
    ThoiGianGoi  DATETIME      DEFAULT GETDATE(),       -- [MỚI] Giúp bếp biết món nào gọi trước
    GhiChu       NVARCHAR(100) NULL,

    PRIMARY KEY (MaHD, MaMon),
    FOREIGN KEY (MaHD)  REFERENCES HoaDon(MaHD) ON DELETE CASCADE,
    FOREIGN KEY (MaMon) REFERENCES MonAn(MaMon)
)
GO

-- 14. Khuyến Mãi
CREATE TABLE KhuyenMai (
    MaKM             VARCHAR(20)   PRIMARY KEY,
    TenKM            NVARCHAR(100) NOT NULL,
    LoaiKM           NVARCHAR(20)  CHECK (LoaiKM IN (N'Giảm %', N'Giảm tiền', N'Tặng món')),
    GiaTri           DECIMAL(18,2) NULL,
    DieuKienToiThieu DECIMAL(18,2) DEFAULT 0,
    NgayBatDau       DATETIME      NOT NULL,
    NgayKetThuc      DATETIME      NOT NULL,
    TrangThai        NVARCHAR(20)  DEFAULT N'Đang hoạt động',
    HangVIPApDung    NVARCHAR(20)  NULL, 

    CONSTRAINT CK_KM_ThoiGian CHECK (NgayKetThuc > NgayBatDau)
)
GO

-- 15. Hóa Đơn - Khuyến Mãi
CREATE TABLE HoaDon_KhuyenMai (
    MaHD    INT           NOT NULL,
    MaKM    VARCHAR(20)   NOT NULL,
    GiamGia DECIMAL(18,2) NULL,

    PRIMARY KEY (MaHD, MaKM),
    FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD),
    FOREIGN KEY (MaKM) REFERENCES KhuyenMai(MaKM)
)
GO

-- 16. Lịch Sử Bàn
CREATE TABLE LichSuBan (
    MaLichSu        INT IDENTITY(1,1) PRIMARY KEY,
    MaBan           VARCHAR(10)   NOT NULL,
    ThoiGianBatDau  DATETIME      NOT NULL,
    ThoiGianKetThuc DATETIME      NULL,
    SoLuongKhach    INT           NULL,
    DoanhThu        DECIMAL(18,2) DEFAULT 0,
    MaHD            INT           NULL,

    FOREIGN KEY (MaBan) REFERENCES Ban(MaBan),
    FOREIGN KEY (MaHD)  REFERENCES HoaDon(MaHD)
)
GO


/* ====================================================
   TRIGGERS
   ==================================================== */

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
            ELSE                          N'Đồng'
        END
        WHERE SoDienThoai IN (SELECT SoDienThoai FROM inserted)
    END
END
GO

-- Trigger 2: Link DatBan → HoaDon khi tạo hóa đơn mới
CREATE TRIGGER TR_LinkDatBanHoaDon
ON HoaDon
AFTER INSERT
AS
BEGIN
    UPDATE DatBan
    SET MaHD      = i.MaHD,
        TrangThai = N'Hoàn thành'
    FROM DatBan db
    INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat
    INNER JOIN inserted i         ON ctdb.MaBan = i.MaBan
    WHERE db.TrangThai = N'Đã nhận bàn'
      AND GETDATE() BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc
END
GO

-- Trigger 3: Tự động cập nhật TongTien khi chi tiết thay đổi
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


/* ====================================================
   STORED PROCEDURES
   ==================================================== */

-- SP 1: Kiểm tra xung đột đặt bàn
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
      AND (@MaDatHienTai IS NULL OR db.MaDat <> @MaDatHienTai)
      AND db.TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã nhận bàn')
      AND (
          @ThoiGianBatDau  BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc
          OR @ThoiGianKetThuc BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc
          OR db.ThoiGianBatDau BETWEEN @ThoiGianBatDau AND @ThoiGianKetThuc
      )
END
GO

-- SP 2: Gợi ý bàn phù hợp số khách
CREATE PROCEDURE SP_GoiYBan
    @SoKhach         INT,
    @ThoiGianBatDau  DATETIME,
    @ThoiGianKetThuc DATETIME,
    @MaKV            VARCHAR(10) = NULL
AS
BEGIN
    SELECT TOP 10
        b.MaBan, b.TenBan, b.SoGhe, kv.TenKV,
        ABS(b.SoGhe - @SoKhach) AS ChenhLech
    FROM Ban b
    JOIN KhuVuc kv ON b.MaKV = kv.MaKV
    WHERE b.SoGhe >= @SoKhach
      AND (@MaKV IS NULL OR b.MaKV = @MaKV)
      AND b.TrangThai <> N'Đang Gộp'
      AND b.MaBan NOT IN (
          SELECT ctdb.MaBan
          FROM DatBan db
          INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat
          WHERE db.TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã nhận bàn')
            AND (
                @ThoiGianBatDau  BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc
                OR @ThoiGianKetThuc BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc
                OR db.ThoiGianBatDau BETWEEN @ThoiGianBatDau AND @ThoiGianKetThuc
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
    DECLARE @ThanhTien DECIMAL(18,2)
    SELECT @ThanhTien = ThanhTien FROM HoaDon WHERE MaHD = @MaHD
    DECLARE @DiemThem INT = FLOOR(@ThanhTien / 10000)

    IF EXISTS (SELECT 1 FROM KhachHang WHERE SoDienThoai = @SDT)
    BEGIN
        UPDATE KhachHang
        SET DiemTichLuy     = DiemTichLuy + @DiemThem,
            LanGiaoDichCuoi = GETDATE(),
            TongChiTieu     = TongChiTieu + @ThanhTien
        WHERE SoDienThoai = @SDT
    END
END
GO

-- SP 4: Tính giảm giá VIP
CREATE PROCEDURE SP_TinhGiamGiaVIP
    @SDT      VARCHAR(20),
    @TongTien DECIMAL(18,2),
    @GiamGia  DECIMAL(18,2) OUTPUT
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

-- SP 5: Đặt bàn (hỗ trợ nhiều bàn)
CREATE PROCEDURE SP_DatBan
    @TenKhachDat     NVARCHAR(50),
    @SDT             VARCHAR(20),
    @ThoiGianBatDau  DATETIME,
    @ThoiGianKetThuc DATETIME,
    @SoLuongKhach    INT,
    @DanhSachBan     NVARCHAR(MAX),
    @GhiChu          NVARCHAR(200) = NULL,
    @TienCoc         DECIMAL(18,2) = 0,
    @KetQua          INT           OUTPUT,
    @ThongBao        NVARCHAR(200) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION
        DECLARE @BanList TABLE (MaBan VARCHAR(10))
        INSERT INTO @BanList
        SELECT LTRIM(RTRIM(value)) FROM STRING_SPLIT(@DanhSachBan, ',') WHERE LTRIM(RTRIM(value)) <> ''

        DECLARE @XungDot INT = 0
        SELECT @XungDot = COUNT(*) FROM @BanList bl
        WHERE EXISTS (
            SELECT 1 FROM DatBan db WITH (UPDLOCK)
            INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat
            WHERE ctdb.MaBan = bl.MaBan
              AND db.TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã nhận bàn')
              AND (
                  @ThoiGianBatDau  BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc
                  OR @ThoiGianKetThuc BETWEEN db.ThoiGianBatDau AND db.ThoiGianKetThuc
                  OR db.ThoiGianBatDau BETWEEN @ThoiGianBatDau AND @ThoiGianKetThuc
              )
        )

        IF @XungDot > 0
        BEGIN
            ROLLBACK TRANSACTION
            SET @KetQua   = 0
            SET @ThongBao = N'Một hoặc nhiều bàn đã bị đặt!'
            RETURN
        END

        INSERT INTO DatBan (TenKhachDat, SDT, ThoiGianBatDau, ThoiGianKetThuc, SoLuongKhach, GhiChu, TienCoc, TrangThai)
        VALUES (@TenKhachDat, @SDT, @ThoiGianBatDau, @ThoiGianKetThuc, @SoLuongKhach, @GhiChu, @TienCoc, N'Chờ xác nhận')

        DECLARE @MaDatMoi INT = SCOPE_IDENTITY()
        INSERT INTO ChiTietDatBan (MaDat, MaBan) SELECT @MaDatMoi, MaBan FROM @BanList

        COMMIT TRANSACTION
        SET @KetQua   = 1
        SET @ThongBao = N'Thành công! Mã đặt: ' + CAST(@MaDatMoi AS VARCHAR)
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION
        SET @KetQua   = -1
        SET @ThongBao = N'Lỗi: ' + ERROR_MESSAGE()
    END CATCH
END
GO

-- SP 6: Thống kê doanh thu theo khoảng ngày [ĐÃ CẬP NHẬT TRẠNG THÁI]
CREATE PROCEDURE SP_ThongKeDoanhThu
    @NgayBatDau DATE,
    @NgayKetThuc DATE
AS
BEGIN
    SELECT 
        CAST(ThoiGianThanhToan AS DATE) AS Ngay,
        COUNT(MaHD)            AS SoHoaDon,
        SUM(SoLuongKhach)      AS TongKhach,
        SUM(ThanhTien)         AS DoanhThu
    FROM HoaDon
    WHERE TrangThai = N'Đã thanh toán'
      AND CAST(ThoiGianThanhToan AS DATE) BETWEEN @NgayBatDau AND @NgayKetThuc
    GROUP BY CAST(ThoiGianThanhToan AS DATE)
    ORDER BY Ngay DESC
END
GO

-- SP 7: Lấy giá hiện tại (cho 1 món cụ thể)
CREATE PROCEDURE SP_LayGiaHienTai
    @MaMon    VARCHAR(10),
    @ThoiDiem DATETIME = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF @ThoiDiem IS NULL SET @ThoiDiem = GETDATE()

    DECLARE @GiaKetQua DECIMAL(18,2)

    SELECT TOP 1 @GiaKetQua = ctbg.DonGia
    FROM ChiTietBangGia ctbg
    INNER JOIN BangGia bg ON ctbg.MaBG = bg.MaBG
    WHERE ctbg.MaMon   = @MaMon
      AND bg.TrangThai = N'Hoạt động'
      AND (bg.NgayBatDau    IS NULL OR CAST(@ThoiDiem AS DATE) >= bg.NgayBatDau)
      AND (bg.NgayKetThuc   IS NULL OR CAST(@ThoiDiem AS DATE) <= bg.NgayKetThuc)
      AND (bg.GioBatDau  IS NULL OR CAST(@ThoiDiem AS TIME) >= bg.GioBatDau)
      AND (bg.GioKetThuc IS NULL OR CAST(@ThoiDiem AS TIME) <= bg.GioKetThuc)
    ORDER BY bg.UuTien DESC

    IF @GiaKetQua IS NULL
        SELECT @GiaKetQua = DonGia FROM MonAn WHERE MaMon = @MaMon

    SELECT ISNULL(@GiaKetQua, 0) AS DonGia
END
GO

-- SP 8: Thanh toán hóa đơn [ĐÃ CẬP NHẬT PT THANH TOÁN]
CREATE PROCEDURE SP_ThanhToan
    @MaHD                INT,
    @MaKM                VARCHAR(20)  = NULL,
    @SDT                 VARCHAR(20)  = NULL,
    @VAT                 DECIMAL(5,2) = 10,
    @PhiPhucVu           DECIMAL(5,2) = 5,
    @PhuongThucThanhToan NVARCHAR(50) = N'Tiền mặt' -- [MỚI]
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @TongTienMon DECIMAL(18,2)
    SELECT @TongTienMon = ISNULL(SUM(SoLuong * DonGia), 0) FROM ChiTietHoaDon WHERE MaHD = @MaHD

    DECLARE @GiamVIP DECIMAL(18,2) = 0
    IF @SDT IS NOT NULL EXEC SP_TinhGiamGiaVIP @SDT, @TongTienMon, @GiamVIP OUTPUT

    DECLARE @GiamKM DECIMAL(18,2) = 0
    IF @MaKM IS NOT NULL
    BEGIN
        DECLARE @LoaiKM NVARCHAR(20), @GiaTriKM DECIMAL(18,2), @DieuKien DECIMAL(18,2)
        SELECT @LoaiKM = LoaiKM, @GiaTriKM = GiaTri, @DieuKien = DieuKienToiThieu
        FROM KhuyenMai WHERE MaKM = @MaKM AND TrangThai = N'Đang hoạt động' AND GETDATE() BETWEEN NgayBatDau AND NgayKetThuc

        IF @GiaTriKM IS NOT NULL AND @TongTienMon >= @DieuKien
        BEGIN
            SET @GiamKM = CASE
                WHEN @LoaiKM = N'Giảm %'    THEN @TongTienMon * @GiaTriKM / 100
                WHEN @LoaiKM = N'Giảm tiền' THEN @GiaTriKM
                ELSE 0 END
        END
    END

    DECLARE @TongGiam DECIMAL(18,2) = @GiamVIP + @GiamKM
    DECLARE @ThanhTien DECIMAL(18,2) = @TongTienMon * (1 + @VAT / 100 + @PhiPhucVu / 100) - @TongGiam
    IF @ThanhTien < 0 SET @ThanhTien = 0

    UPDATE HoaDon
    SET TongTien            = @TongTienMon,
        PhanTramVAT         = @VAT,
        PhiPhucVu           = @PhiPhucVu,
        TienGiamGia         = @TongGiam,
        ThanhTien           = @ThanhTien,
        TrangThai           = N'Đã thanh toán',          -- [MỚI]
        PhuongThucThanhToan = @PhuongThucThanhToan,      -- [MỚI]
        ThoiGianThanhToan   = GETDATE(),                 -- [MỚI]
        SDT_Khach           = ISNULL(@SDT, SDT_Khach)
    WHERE MaHD = @MaHD

    IF @MaKM IS NOT NULL AND @GiamKM > 0
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM HoaDon_KhuyenMai WHERE MaHD = @MaHD AND MaKM = @MaKM)
            INSERT INTO HoaDon_KhuyenMai (MaHD, MaKM, GiamGia) VALUES (@MaHD, @MaKM, @GiamKM)
    END

    IF @SDT IS NOT NULL EXEC SP_TichDiem @MaHD, @SDT

    SELECT @TongTienMon AS TongTienMon, @TongGiam AS TongGiam, @ThanhTien AS ThanhTien
END
GO

-- SP 9: [MỚI] Lấy Menu đầy đủ kèm giá hiện hành (Dùng để load giao diện)
CREATE PROCEDURE SP_LayMenuHienTai
    @ThoiDiem DATETIME = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF @ThoiDiem IS NULL SET @ThoiDiem = GETDATE();

    SELECT 
        ma.MaMon, 
        ma.TenMon, 
        lm.TenLoai,
        ma.HinhAnh,
        ma.TrangThai,
        COALESCE(
            (SELECT TOP 1 ctbg.DonGia
             FROM ChiTietBangGia ctbg
             INNER JOIN BangGia bg ON ctbg.MaBG = bg.MaBG
             WHERE ctbg.MaMon = ma.MaMon
               AND bg.TrangThai = N'Hoạt động'
               AND (bg.NgayBatDau IS NULL OR CAST(@ThoiDiem AS DATE) >= bg.NgayBatDau)
               AND (bg.NgayKetThuc IS NULL OR CAST(@ThoiDiem AS DATE) <= bg.NgayKetThuc)
               AND (bg.GioBatDau IS NULL OR CAST(@ThoiDiem AS TIME) >= bg.GioBatDau)
               AND (bg.GioKetThuc IS NULL OR CAST(@ThoiDiem AS TIME) <= bg.GioKetThuc)
             ORDER BY bg.UuTien DESC), 
        ma.DonGia) AS GiaHienTai
    FROM MonAn ma
    INNER JOIN LoaiMon lm ON ma.MaLoai = lm.MaLoai
    WHERE ma.TrangThai <> N'Ngừng phục vụ'
    ORDER BY lm.TenLoai, ma.TenMon
END
GO

/* ====================================================
   DỮ LIỆU MẪU CƠ BẢN
   ==================================================== */

USE QuanLyNhaHang__V3
GO

-- ============================================================
-- XÓA DỮ LIỆU CŨ (Nếu có, chạy theo thứ tự để không dính khóa ngoại)
-- ============================================================
DELETE FROM LichSuBan;
DELETE FROM HoaDon_KhuyenMai;
DELETE FROM ChiTietHoaDon;
DELETE FROM HoaDon;
DELETE FROM ChiTietDatBan;
DELETE FROM DatBan;
DELETE FROM KhachHang;
DELETE FROM ChiTietBangGia;
DELETE FROM BangGia;
DELETE FROM MonAn;
DELETE FROM LoaiMon;
DELETE FROM Ban;
DELETE FROM KhuVuc;
DELETE FROM TaiKhoan;
DELETE FROM NhanVien;
DELETE FROM KhuyenMai;
GO

-- ============================================================
-- 1. NHÂN VIÊN & TÀI KHOẢN (Mở rộng thêm bếp, phục vụ)
-- ============================================================
INSERT INTO NhanVien (MaNV, TenNV, GioiTinh, SoDienThoai, Email, CCCD, TrangThai) VALUES
('admin', N'Cao Trọng Nguyễn', N'Nam', '0901234567', 'nguyen.admin@nhahang.vn', '079099112233', N'Đang làm việc'),
('nv01',  N'Trần Thị Thu Thủy', N'Nữ', '0912345678', 'thuy.tran@nhahang.vn',   '079099223344', N'Đang làm việc'),
('nv02',  N'Lê Hoàng Long',     N'Nam', '0923456789', 'long.le@nhahang.vn',     '079099334455', N'Đang làm việc'),
('nv03',  N'Phạm Mai Phương',   N'Nữ', '0934567890', 'phuong.pham@nhahang.vn', '079099445566', N'Đang làm việc'),
('nv04',  N'Nguyễn Văn Bếp',    N'Nam', '0945678901', 'bep.nguyen@nhahang.vn',  '079099556677', N'Đang làm việc'),
('nv05',  N'Vũ Khắc Tiệp',      N'Nam', '0956789012', NULL,                     '079099667788', N'Đã nghỉ');
GO

INSERT INTO TaiKhoan (MaTK, MatKhau, VaiTro) VALUES
('admin', '123', N'Quản lý'),
('nv01',  '123', N'Thu ngân'),
('nv02',  '123', N'Nhân viên'),
('nv03',  '123', N'Nhân viên'),
('nv04',  '123', N'Đầu bếp');
GO

-- ============================================================
-- 2. KHU VỰC & BÀN (Thêm khu sân vườn, tổng 20 bàn)
-- ============================================================
INSERT INTO KhuVuc (MaKV, TenKV, MoTa) VALUES
('KV01', N'Sảnh Trong (Tầng 1)', N'Không gian máy lạnh, bàn nhỏ'),
('KV02', N'Sân Thượng (Tầng 2)', N'Không gian mở, view thành phố'),
('KV03', N'Phòng VIP',           N'Phòng riêng biệt, cách âm'),
('KV04', N'Sân Vườn',            N'Không gian xanh, thoáng mát');
GO

INSERT INTO Ban (MaBan, TenBan, TrangThai, MaKV, SoGhe) VALUES
-- KV01 (8 bàn)
('T1-01', N'Bàn T1-01', N'Trống', 'KV01', 2), ('T1-02', N'Bàn T1-02', N'Trống', 'KV01', 2),
('T1-03', N'Bàn T1-03', N'Trống', 'KV01', 4), ('T1-04', N'Bàn T1-04', N'Trống', 'KV01', 4),
('T1-05', N'Bàn T1-05', N'Trống', 'KV01', 4), ('T1-06', N'Bàn T1-06', N'Trống', 'KV01', 4),
('T1-07', N'Bàn T1-07', N'Trống', 'KV01', 6), ('T1-08', N'Bàn T1-08', N'Trống', 'KV01', 6),
-- KV02 (6 bàn)
('T2-01', N'Bàn T2-01', N'Trống', 'KV02', 4), ('T2-02', N'Bàn T2-02', N'Trống', 'KV02', 4),
('T2-03', N'Bàn T2-03', N'Trống', 'KV02', 6), ('T2-04', N'Bàn T2-04', N'Trống', 'KV02', 6),
('T2-05', N'Bàn T2-05', N'Trống', 'KV02', 8), ('T2-06', N'Bàn T2-06', N'Trống', 'KV02', 8),
-- KV03 (Phòng VIP)
('V-01',  N'VIP 01',    N'Trống', 'KV03', 10), ('V-02',  N'VIP 02',    N'Trống', 'KV03', 15),
('V-03',  N'VIP Lớn',   N'Trống', 'KV03', 30),
-- KV04 (Sân vườn)
('SV-01', N'Bàn SV-01', N'Trống', 'KV04', 4), ('SV-02', N'Bàn SV-02', N'Trống', 'KV04', 6),
('SV-03', N'Bàn SV-03', N'Trống', 'KV04', 8);
GO

-- ============================================================
-- 3. MENU (Loại Món & 25 Món Ăn đa dạng)
-- ============================================================
INSERT INTO LoaiMon (MaLoai, TenLoai, MoTa) VALUES
('L01', N'Khai vị',     N'Kích thích vị giác'),
('L02', N'Món chính',   N'Thịt, hải sản, lẩu'),
('L03', N'Cơm - Mì',    N'Chắc bụng'),
('L04', N'Đồ uống',     N'Bia, nước ngọt, juice'),
('L05', N'Tráng miệng', N'Kem, trái cây, bánh');
GO

INSERT INTO MonAn (MaMon, TenMon, DonViTinh, DonGia, HinhAnh, MaLoai, TrangThai) VALUES
-- Khai vị
('M01', N'Khoai tây chiên bơ tỏi', N'Dĩa',  55000, 'khoaitay.jpg', 'L01', N'Còn món'),
('M02', N'Gỏi ngó sen tôm thịt',   N'Dĩa',  95000, 'goi.jpg',      'L01', N'Còn món'),
('M03', N'Súp cua tuyết nhĩ',      N'Chén', 45000, 'supcua.jpg',   'L01', N'Còn món'),
('M04', N'Salad cá ngừ',           N'Dĩa',  85000, 'salad.jpg',    'L01', N'Còn món'),
('M05', N'Đậu hũ lướt ván',        N'Dĩa',  60000, 'dauhu.jpg',    'L01', N'Còn món'),
-- Món chính
('M06', N'Bò bít tết xốt tiêu',    N'Phần', 180000, 'beefsteak.jpg','L02', N'Còn món'),
('M07', N'Lẩu Thái hải sản',       N'Nồi',  280000, 'lauthai.jpg',  'L02', N'Còn món'),
('M08', N'Lẩu nấm chim câu',       N'Nồi',  320000, 'launam.jpg',   'L02', N'Còn món'),
('M09', N'Gà ta nướng muối ớt',    N'Con',  220000, 'ganuong.jpg',  'L02', N'Còn món'),
('M10', N'Cua gạch rang me',       N'Kg',   650000, 'cuarang.jpg',  'L02', N'Còn món'),
('M11', N'Mực sữa chiên nước mắm', N'Dĩa',  150000, 'mucchiem.jpg', 'L02', N'Còn món'),
('M12', N'Hàu nướng phô mai',      N'Con',  25000,  'hau.jpg',      'L02', N'Còn món'),
-- Cơm mì
('M13', N'Cơm chiên hải sản',      N'Dĩa',  120000, 'comchien.jpg', 'L03', N'Còn món'),
('M14', N'Mì xào giòn thập cẩm',   N'Dĩa',  110000, 'mixao.jpg',    'L03', N'Còn món'),
('M15', N'Miến xào cua',           N'Dĩa',  160000, 'mienxao.jpg',  'L03', N'Còn món'),
-- Đồ uống
('M16', N'Tiger Beer (Lon)',       N'Lon',  22000,  'tiger.jpg',    'L04', N'Còn món'),
('M17', N'Heineken (Chai)',        N'Chai', 25000,  'heineken.jpg', 'L04', N'Còn món'),
('M18', N'Coca Cola',              N'Lon',  18000,  'coca.jpg',     'L04', N'Còn món'),
('M19', N'Nước suối Aquafina',     N'Chai', 15000,  'nuocsuoi.jpg', 'L04', N'Còn món'),
('M20', N'Nước ép dưa hấu',        N'Ly',   45000,  'ephau.jpg',    'L04', N'Còn món'),
('M21', N'Mojito Chanh Dây',       N'Ly',   55000,  'mojito.jpg',   'L04', N'Còn món'),
-- Tráng miệng
('M22', N'Trái cây dĩa (Nhỏ)',     N'Dĩa',  70000,  'traicay.jpg',  'L05', N'Còn món'),
('M23', N'Trái cây dĩa (Lớn)',     N'Dĩa',  120000, 'traicay.jpg',  'L05', N'Còn món'),
('M24', N'Kem Vani hạnh nhân',     N'Ly',   45000,  'kem.jpg',      'L05', N'Còn món'),
('M25', N'Chè khúc bạch',          N'Chén', 35000,  'che.jpg',      'L05', N'Hết món');
GO

-- ============================================================
-- 4. BẢNG GIÁ & CHI TIẾT
-- ============================================================
INSERT INTO BangGia (TenBG, LoaiBG, UuTien, TrangThai) VALUES
(N'Giá niêm yết 2026', N'Thường', 0, N'Hoạt động');

-- Đổ toàn bộ giá mặc định từ MonAn sang ChiTietBangGia
INSERT INTO ChiTietBangGia (MaBG, MaMon, DonGia)
SELECT 1, MaMon, DonGia FROM MonAn;

-- Bảng giá Happy Hour (Giảm giá bia, giờ vàng)
INSERT INTO BangGia (TenBG, LoaiBG, GioBatDau, GioKetThuc, UuTien, TrangThai) VALUES
(N'Happy Hour - Giờ Vàng', N'HappyHour', '16:00:00', '19:00:00', 5, N'Hoạt động');

INSERT INTO ChiTietBangGia (MaBG, MaMon, DonGia, GhiChu) VALUES
(2, 'M16', 18000, N'Tiger HH'),
(2, 'M17', 20000, N'Ken HH'),
(2, 'M21', 40000, N'Mojito HH');
GO

-- ============================================================
-- 5. KHÁCH HÀNG & KHUYẾN MÃI
-- ============================================================
INSERT INTO KhachHang (SoDienThoai, TenKhach, HangVIP, DiemTichLuy, TongChiTieu) VALUES
('0988111222', N'Đặng Văn Sơn',   N'Kim cương', 1500, 15000000),
('0977333444', N'Lý Thảo Nguyên', N'Vàng',      600,  6000000),
('0966555666', N'Trương Vô Kỵ',   N'Bạc',       250,  2500000),
('0955777888', N'Triệu Mẫn',      N'Đồng',      50,   500000),
('0900111000', N'Khách vãng lai', N'Đồng',      0,    0);

INSERT INTO KhuyenMai (MaKM, TenKM, LoaiKM, GiaTri, DieuKienToiThieu, NgayBatDau, NgayKetThuc, TrangThai) VALUES
('KM_HE2026', N'Chào hè 2026 - Giảm 10%',   N'Giảm %',    10,     500000,  '2026-03-01', '2026-05-31', N'Đang hoạt động'),
('KM_MOMO',   N'Thanh toán Momo giảm 50K', N'Giảm tiền', 50000,  300000,  '2026-03-01', '2026-04-30', N'Đang hoạt động');
GO

-- ============================================================
-- 6. DỮ LIỆU ĐẶT BÀN
-- ============================================================
-- Đặt bàn cho tối nay
INSERT INTO DatBan (TenKhachDat, SDT, ThoiGianBatDau, ThoiGianKetThuc, SoLuongKhach, TrangThai, TienCoc) VALUES
(N'Anh Sơn', '0988111222', DATEADD(HOUR, 19, CAST(GETDATE() AS DATE)), DATEADD(HOUR, 22, CAST(GETDATE() AS DATE)), 10, N'Đã xác nhận', 500000),
(N'Chị Nguyên', '0977333444', DATEADD(HOUR, 18, CAST(GETDATE() AS DATE)), DATEADD(HOUR, 20, CAST(GETDATE() AS DATE)), 4, N'Chờ xác nhận', 0);

INSERT INTO ChiTietDatBan (MaDat, MaBan) VALUES (1, 'V-01'), (2, 'SV-01');
GO

-- ============================================================
-- 7. LỊCH SỬ HÓA ĐƠN ĐỂ TEST THỐNG KÊ (Giả lập bill 3 ngày qua)
-- ============================================================
-- Ngày hôm kia (22/03/2026)
INSERT INTO HoaDon (NgayTao, TrangThai, ThoiGianThanhToan, PhuongThucThanhToan, MaBan, SDT_Khach, MaNV) VALUES
('2026-03-22 18:00:00', N'Đã thanh toán', '2026-03-22 20:30:00', N'Tiền mặt',     'T1-01', NULL,         'nv01'),
('2026-03-22 19:15:00', N'Đã thanh toán', '2026-03-22 21:00:00', N'Chuyển khoản', 'T2-03', '0966555666', 'nv01');

INSERT INTO ChiTietHoaDon (MaHD, MaMon, SoLuong, DonGia, TrangThaiMon) VALUES
(1, 'M06', 2, 180000, N'Đã phục vụ'), (1, 'M16', 4, 22000, N'Đã phục vụ'), 
(2, 'M07', 1, 280000, N'Đã phục vụ'), (2, 'M11', 1, 150000, N'Đã phục vụ'), (2, 'M20', 3, 45000, N'Đã phục vụ');

-- Ngày hôm qua (23/03/2026)
INSERT INTO HoaDon (NgayTao, TrangThai, ThoiGianThanhToan, PhuongThucThanhToan, MaBan, SDT_Khach, MaNV) VALUES
('2026-03-23 11:30:00', N'Đã thanh toán', '2026-03-23 13:00:00', N'Momo',         'T1-05', '0955777888', 'nv02'),
('2026-03-23 18:30:00', N'Đã thanh toán', '2026-03-23 22:00:00', N'Chuyển khoản', 'V-02',  '0988111222', 'nv01');

INSERT INTO ChiTietHoaDon (MaHD, MaMon, SoLuong, DonGia, TrangThaiMon) VALUES
(3, 'M13', 2, 120000, N'Đã phục vụ'), (3, 'M18', 2, 18000, N'Đã phục vụ'),
(4, 'M10', 2, 650000, N'Đã phục vụ'), (4, 'M09', 1, 220000, N'Đã phục vụ'), (4, 'M17', 10, 25000, N'Đã phục vụ');

-- Ngày hôm nay (Hóa đơn đang ăn, chưa thanh toán)
INSERT INTO HoaDon (NgayTao, TrangThai, MaBan, MaNV) VALUES
(GETDATE(), N'Chưa thanh toán', 'SV-03', 'nv02');

INSERT INTO ChiTietHoaDon (MaHD, MaMon, SoLuong, DonGia, TrangThaiMon) VALUES
(5, 'M08', 1, 320000, N'Đang nấu'), (5, 'M01', 1, 55000, N'Đã phục vụ'), (5, 'M16', 6, 18000, N'Đã phục vụ');

-- Cập nhật tổng tiền cho các hóa đơn vừa tạo
UPDATE HoaDon
SET TongTien = (SELECT ISNULL(SUM(SoLuong * DonGia), 0) FROM ChiTietHoaDon WHERE MaHD = HoaDon.MaHD);

UPDATE HoaDon
SET ThanhTien = TongTien * 1.1 -- Tạm tính VAT 10%
WHERE TrangThai = N'Đã thanh toán';
GO