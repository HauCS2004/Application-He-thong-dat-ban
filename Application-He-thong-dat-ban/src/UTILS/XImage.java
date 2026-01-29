package UTILS;

import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.ImageIcon;

public class XImage {
    
    // Hàm này giúp lấy icon từ gói VIEW cực nhanh
    public static ImageIcon getIcon(String tenFile) {
        // Cách lấy đường dẫn tương đối từ thư mục src
        // Dấu "/" đầu tiên nghĩa là bắt đầu tìm từ thư mục gốc (src)
        URL url = XImage.class.getResource("/view/" + tenFile);
        
        if (url != null) {
            return new ImageIcon(url);
        } else {
            System.err.println("Không tìm thấy ảnh: " + tenFile);
            return null;
        }
    }
 // HÀM LƯU ẢNH (BẠN ĐANG THIẾU CÁI NÀY)
    public static void save(File src) {
        File dst = new File("src/VIEW", src.getName());
        if (!dst.getParentFile().exists()) {
            dst.getParentFile().mkdirs(); 
        }
        try {
            Path from = Paths.get(src.getAbsolutePath());
            Path to = Paths.get(dst.getAbsolutePath());
            // Copy file vào thư mục dự án
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // HÀM ĐỌC ẢNH
    public static ImageIcon read(String fileName) {
        File path = new File("src/VIEW", fileName);
        return new ImageIcon(path.getAbsolutePath());
    }

    
    public static ImageIcon resize(ImageIcon icon, int width, int height) {
        if (icon == null) return null;
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }
}