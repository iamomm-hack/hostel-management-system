package hotelmanagement.client;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Utility for loading, saving, rendering circular user profile images,
 * and opening the modern native Windows 11 File Explorer dialog.
 */
public final class ProfileImageUtil {

    private static final File AVATARS_DIR = new File("avatars");

    static {
        if (!AVATARS_DIR.exists()) {
            AVATARS_DIR.mkdirs();
        }
    }

    private ProfileImageUtil() {}

    /** Returns the avatar file for a given username. */
    public static File getAvatarFile(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String safeName = username.trim().toLowerCase().replaceAll("[^a-zA-Z0-9_.-]", "_");
        return new File(AVATARS_DIR, safeName + ".png");
    }

    /** Saves an image file as the profile photo for a user. */
    public static boolean saveProfileImage(String username, File sourceFile) {
        if (username == null || sourceFile == null || !sourceFile.exists()) {
            return false;
        }
        try {
            BufferedImage img = ImageIO.read(sourceFile);
            if (img == null) {
                return false;
            }
            File dest = getAvatarFile(username);
            return ImageIO.write(img, "png", dest);
        } catch (IOException e) {
            System.err.println("Could not save avatar: " + e.getMessage());
            return false;
        }
    }

    /** Loads and returns the raw profile image for a username, or null if none exists. */
    public static BufferedImage loadProfileImage(String username) {
        File file = getAvatarFile(username);
        if (file != null && file.exists()) {
            try {
                return ImageIO.read(file);
            } catch (IOException ignored) {}
        }
        return null;
    }

    /** Returns a smoothly scaled circular BufferedImage of the given diameter. */
    public static BufferedImage createCircularAvatar(BufferedImage source, int diameter) {
        if (source == null || diameter <= 0) {
            return null;
        }
        BufferedImage output = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Clip to circle
        g2.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));

        // Center and scale image proportionally
        int w = source.getWidth();
        int h = source.getHeight();
        double scale = Math.max((double) diameter / w, (double) diameter / h);
        int scaledW = (int) (w * scale);
        int scaledH = (int) (h * scale);
        int x = (diameter - scaledW) / 2;
        int y = (diameter - scaledH) / 2;

        g2.drawImage(source, x, y, scaledW, scaledH, null);
        g2.dispose();
        return output;
    }

    /**
     * Opens the modern native Windows 11 File Explorer picker (IFileOpenDialog),
     * ensuring the true Windows 11 Fluent interface opens rather than legacy Win32/Java dialogs.
     */
    public static File pickImageFile(Component parent) {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "powershell.exe",
                        "-NoProfile",
                        "-ExecutionPolicy", "Bypass",
                        "-STA",
                        "-Command",
                        "Add-Type -AssemblyName System.Windows.Forms; "
                        + "$f = New-Object System.Windows.Forms.OpenFileDialog; "
                        + "$f.Title = 'Select Profile Photo'; "
                        + "$f.Filter = 'Image Files (*.jpg;*.jpeg;*.png)|*.jpg;*.jpeg;*.png|All Files (*.*)|*.*'; "
                        + "$f.RestoreDirectory = $true; "
                        + "$f.AutoUpgradeEnabled = $true; "
                        + "if ($f.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) { Write-Output $f.FileName }"
                );
                pb.redirectErrorStream(false);
                Process process = pb.start();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line = reader.readLine();
                    process.waitFor();
                    if (line != null && !line.trim().isEmpty()) {
                        File file = new File(line.trim());
                        if (file.exists()) {
                            return file;
                        }
                    }
                    return null;
                }
            } catch (Exception ex) {
                System.err.println("Native Windows 11 picker fallback: " + ex.getMessage());
            }
        }

        // Cross-platform fallback
        FileDialog fileDialog;
        if (parent instanceof Dialog) {
            fileDialog = new FileDialog((Dialog) parent, "Select Profile Photo", FileDialog.LOAD);
        } else if (parent instanceof Frame) {
            fileDialog = new FileDialog((Frame) parent, "Select Profile Photo", FileDialog.LOAD);
        } else {
            fileDialog = new FileDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Select Profile Photo", FileDialog.LOAD);
        }
        fileDialog.setFile("*.jpg;*.jpeg;*.png");
        fileDialog.setVisible(true);

        String filename = fileDialog.getFile();
        String directory = fileDialog.getDirectory();
        if (filename != null && directory != null) {
            return new File(directory, filename);
        }
        return null;
    }
}
