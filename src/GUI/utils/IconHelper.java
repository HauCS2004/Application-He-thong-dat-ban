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
     * Resizes an icon to specific width/height
     */
    public static ImageIcon resize(ImageIcon icon, int width, int height) {
        if (icon == null)
            return null;
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }
}
