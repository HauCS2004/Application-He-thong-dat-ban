package GUI.utils;

import javax.swing.ImageIcon;
import java.net.URL;
import java.io.File;
import java.awt.Image;

public class IconHelper {
    /**
     * Safely loads an icon from a path (URL or File).
     * 
     * @param path The path to the icon (can be http URL or local file path)
     * @return ImageIcon if found, null otherwise
     */
    public static ImageIcon loadIcon(String path) {
        if (path == null || path.isEmpty())
            return null;

        try {
            // 1. Try as URL (http/https)
            if (path.startsWith("http")) {
                return new ImageIcon(java.net.URI.create(path).toURL());
            }

            // 2. Try as local file
            File f = new File(path);
            if (f.exists()) {
                return new ImageIcon(path);
            }

            // 3. Try as resource (classpath) - Useful for bundled jars
            URL resource = IconHelper.class.getClassLoader().getResource(path);
            if (resource != null) {
                return new ImageIcon(resource);
            }

            // 4. Try loading from src/ if running in IDE (fallback)
            File srcFile = new File("src/" + path);
            if (srcFile.exists()) {
                return new ImageIcon(srcFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.err.println("Could not load icon: " + path + " (" + e.getMessage() + ")");
        }
        return null; // Return null if failed
    }

    /**
     * Resizes an icon to specific width/height, utilizing High-DPI (HiDPI)
     * crispness.
     * By overriding paintIcon, the original high-res image is drawn directly to the
     * scaled Graphics context.
     */
    public static ImageIcon resize(ImageIcon icon, int width, int height) {
        if (icon == null)
            return null;
        return new ScalableImageIcon(icon.getImage(), width, height);
    }

    /**
     * Custom ImageIcon that draws its underlying high-resolution image
     * into the logical bounds, ensuring maximum sharpness on scaled displays (125%,
     * 150%, etc).
     */
    public static class ScalableImageIcon extends ImageIcon {
        private int logicalWidth;
        private int logicalHeight;

        public ScalableImageIcon(Image image, int logicalWidth, int logicalHeight) {
            super(image);
            this.logicalWidth = logicalWidth;
            this.logicalHeight = logicalHeight;
        }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            // Bật thuật toán nén ảnh tốt nhất Bicubic / Khử răng cưa
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            // Vẽ ảnh gốc to trực tiếp lên tọa độ logic, Java sẽ tự động scale cho màn hình
            // HD phân giải cao
            g2.drawImage(getImage(), x, y, logicalWidth, logicalHeight, null);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return logicalWidth;
        }

        @Override
        public int getIconHeight() {
            return logicalHeight;
        }
    }
}
