import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static void openFile(Path filePath) {
        File file = filePath.toFile();

        if (file.exists() && file.isFile()) {
            try {
            	Desktop.getDesktop().open(file);
            } catch (Exception e) {
                System.out.println("Error opening file: " + e.getMessage());
            }
        } else {
            System.out.println("File not found or it's not a valid file.");
        }
    }

    public static void deleteFile(Path filePath) {
        File file = filePath.toFile();

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File or directory deleted: " + file.getName());
            } else {
                System.out.println("Failed to delete file or directory: " + file.getName());
            }
        } else {
            System.out.println("File or directory not found: " + file.getName());
        }
    }


    public static List<String> getFileProperties(Path filePath) {
        File file = filePath.toFile();
        List<String> fileProperties = new ArrayList<>();

        if (file.exists()) {
            fileProperties.add("File Name: " + file.getName());
            fileProperties.add("Path: " + file.getAbsolutePath());
            fileProperties.add("Size: " + file.length() + " bytes");
            fileProperties.add("Last Modified: " + file.lastModified());
            fileProperties.add("Is Directory: " + file.isDirectory());
        } else {
            fileProperties.add("File not found: " + file.getName());
        }

        return fileProperties;  
    }

    public static void moveFile(Path srcPath, Path dstPath) {
        try {
            Files.move(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved from " + srcPath + " to " + dstPath);
        } catch (IOException e) {
            System.out.println("Failed to move file: " + e.getMessage());
        }
    }

    public static void renameFile(Path oldFilePath, Path newFilePath) throws IOException {
        // Check if the file exists
        if (!Files.exists(oldFilePath)) {
            throw new IllegalArgumentException("The specified file does not exist.");
        }

        // Check if the new file path already exists
        if (Files.exists(newFilePath)) {
            throw new IOException("A file with the new name already exists.");
        }

        // Rename the file using Files.move
        Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File renamed from " + oldFilePath + " to " + newFilePath);
    }

    
}

