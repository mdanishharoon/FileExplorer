import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FolderManager {

    private Path currentDirectory;

    // Constructor to initialize with a starting directory
    public FolderManager() {
    }
    
    public FolderManager(Path startingDirectory) {
        if (Files.isDirectory(startingDirectory)) {
            this.currentDirectory = startingDirectory;
        } else {
            throw new IllegalArgumentException("Starting directory is not valid.");
        }
    }

    
    //getter and setters
    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(Path currentDirectory) {
        this.currentDirectory = currentDirectory;
    }
    
    // Method to change the current directory
    public void changeDirectory(Path newDirectory) {
        if (Files.isDirectory(newDirectory)) {
            this.currentDirectory = newDirectory;
        } else {
            throw new IllegalArgumentException("New directory is not valid.");
        }
    }
    
    

    // returns list<string> of dir contents
    public List<String> listDirectoryContents() {
        File dir = currentDirectory.toFile();
        List<String> contentsList = new ArrayList<>();

        if (dir.isDirectory()) {
            String[] listing = dir.list();
            if (listing != null) {
                for (String fileOrDir : listing) {
                    contentsList.add(fileOrDir);
                }
            }
        } else {
            throw new IllegalArgumentException("The current path is not a directory.");
        }

        return contentsList;
    }
    
    

    // Method to rename a folder
    public void renameFolder(Path oldFolder, String newName) throws IOException {
        if (Files.isDirectory(oldFolder)) {
            Path newFolder = oldFolder.resolveSibling(newName); // Create new path with new name
            Files.move(oldFolder, newFolder);
        } else {
            throw new IllegalArgumentException("The provided path is not a folder.");
        }
    }

    //delete files by adding to stack. if subfolder exists 
    
    
    public void deleteFolder(Path folderToDelete) throws IOException {
        File folder = folderToDelete.toFile();
        
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("The provided path is not a folder.");
        }

        List<File> stack = new ArrayList<>();
        stack.add(folder);

        while (!stack.isEmpty()) {
            File currentFolder = stack.get(stack.size() - 1);
            File[] files = currentFolder.listFiles();

            if (files == null || files.length == 0) {
            	//no files in folder
                stack.remove(stack.size() - 1);
                currentFolder.delete();
            } else {
            		//if its file delete otherwise add folderes to stack
            	for (File file : files) {
                    if (file.isDirectory()) {
                        stack.add(file);
                    } else {
                        file.delete();  
                    }
                }
            }
        }
    }

    
    public List<String> getFolderProperties(Path folderPath) {
        List<String> properties = new ArrayList<>();
        try {
            if (Files.isDirectory(folderPath)) {
                properties.add("Folder Name: " + folderPath.getFileName().toString());
                properties.add("Path: " + folderPath.toAbsolutePath().toString());
                properties.add("Number of Files: " + Files.list(folderPath).count());

                // Get last modified time
                BasicFileAttributes attrs = Files.readAttributes(folderPath, BasicFileAttributes.class);
                properties.add("Last Modified: " + attrs.lastModifiedTime());
            } else {
                properties.add("The provided path is not a directory.");
            }
        } catch (IOException e) {
            properties.add("Error retrieving properties: " + e.getMessage());
        }
        return properties;
    }
    
}
