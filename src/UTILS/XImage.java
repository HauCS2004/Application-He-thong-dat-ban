package UTILS;

import java.awt.Image;
import java.io.File;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.ImageIcon;

public class XImage {

    // Đường dẫn thư mục lưu ảnh bên ngoài (Cùng cấp với project/file jar)
    private static final String IMAGE_DIR = "images";

    static {
        // Tạo thư mục images nếu chưa có
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Lưu file ảnh vào thư mục images bên ngoài dự án
     * 
     * @param src File ảnh nguồn được chọn
     * @return boolean thành công hay thất bại
     */
    public static boolean save(File src) {
        File dst = new File(IMAGE_DIR, src.getName());
        try {
            Path from = Paths.get(src.getAbsolutePath());
            Path to = Paths.get(dst.getAbsolutePath());
            // Copy file vào thư mục images (ghi đè nếu có)
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Đọc ảnh từ thư mục images bên ngoài.
     * Nếu không có, thử tìm trong classpath (như cũ).
     * 
     * @param fileName Tên file ảnh (vd: ga.png)
     * @return ImageIcon hoặc null nếu không tìm thấy
     */
    public static ImageIcon read(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        // 1. Ưu tiên tìm trong thư mục images bên ngoài
        File externalFile = new File(IMAGE_DIR, fileName);
        if (externalFile.exists()) {
            return new ImageIcon(externalFile.getAbsolutePath());
        }

        // 2. Nếu không có, tìm trong Resource (classpath) - Hỗ trợ ảnh mặc định của app
        // Lưu ý: Đường dẫn trong code cũ là "/view/image/"
        try {
            return GUI.utils.IconHelper.loadIcon("/view/image/" + fileName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Xóa ảnh trong thư mục images bên ngoài
     * 
     * @param fileName Tên file cần xóa
     */
    public static void delete(String fileName) {
        if (fileName == null || fileName.isEmpty() || fileName.equals("default.png")) {
            return;
        }

        File file = new File(IMAGE_DIR, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static ImageIcon resize(ImageIcon icon, int width, int height) {
        if (icon == null)
            return null;
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }
}