import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.DefaultListModel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.util.List;

import java.util.regex.Pattern;



public class fileExplorer {

    private JFrame frame;
    private JTextField pathField;
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private FolderManager folderManager;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                fileExplorer window = new fileExplorer();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     */
    public fileExplorer() {
        folderManager = new FolderManager(Paths.get(System.getProperty("user.dir"))); // Initialize with the current working directory
        initialize();
    }

    
    private boolean isValidWindowsName(String name) {
        String invalidChars = "[<>:\"/\\\\|?*]";
        return !Pattern.compile(invalidChars).matcher(name).find();
    }
    
    private void updateFileList() {
        listModel.clear(); // Clear existing items
        List<String> contents = folderManager.listDirectoryContents(); // Get updated contents
        for (String item : contents) {
            listModel.addElement(item); // Add each item to the list model
        }
    }
    
    public static PathType checkPathType(Path path) {
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                return PathType.FOLDER;
            } else if (Files.isRegularFile(path)) {
                return PathType.FILE;
            }
        }
        return PathType.INVALID;
    }
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setResizable(false);
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setTitle("File Explorer");
        frame.setIconImage(null);

        pathField = new JTextField();
        pathField.setToolTipText("Enter path");
        pathField.setFont(new Font("Consolas", Font.PLAIN, 16));
        pathField.setBounds(132, 75, 534, 25);
        frame.getContentPane().add(pathField);
        pathField.setColumns(10);

        JLabel lblNewLabel = new JLabel("Enter Path");
        lblNewLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        lblNewLabel.setBounds(33, 75, 98, 25);
        frame.getContentPane().add(lblNewLabel);

        JButton btnGo = new JButton("Go");
        btnGo.setBorder(UIManager.getBorder("Button.border"));
        btnGo.setFont(new Font("Consolas", Font.PLAIN, 16));
        btnGo.setBounds(676, 75, 76, 27);
        frame.getContentPane().add(btnGo);

        // List model to hold file and folder names
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setFont(new Font("Consolas", Font.PLAIN, 16));
        fileList.setBounds(33, 112, 719, 424);
        frame.getContentPane().add(fileList);

        // Setup menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Consolas", Font.PLAIN, 16));
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        
        
        JMenuItem renameItem = new JMenuItem("Rename");
        JMenuItem deleteItem = new JMenuItem("Delete");
        fileMenu.add(renameItem);
        fileMenu.add(deleteItem);
        
        

		btnGo.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String pathInput = pathField.getText().trim();
		        Path newPath = Paths.get(pathInput);
		
		        PathType pathType = checkPathType(newPath);
		
		        switch (pathType) {
		            case FOLDER:
		                try {
		                    folderManager.changeDirectory(newPath);
		                    updateFileList(); // Update the file list in the GUI
		                } catch (IllegalArgumentException ex) {
		                    System.out.println(ex.getMessage());
		                }
		                break;
		            case FILE:
		                FileManager.openFile(newPath);
		                break;
		            default:
		                System.out.println("The path does not point to a valid file or directory.");
		                break;
		        }
		    }
		});

		renameItem.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String selectedItem = (String) fileList.getSelectedValue();
		        
		        if (selectedItem == null) {
		            JOptionPane.showMessageDialog(frame, "Please select a file or folder to rename.");
		            return;
		        }
		
		        Path selectedPath = Paths.get(pathField.getText(), selectedItem);
		        PathType pathType = checkPathType(selectedPath);
		
		        String newName = JOptionPane.showInputDialog(frame, "Enter new name for the selected item:");
		
		        if (newName == null || !isValidWindowsName(newName)) {
		            JOptionPane.showMessageDialog(frame, "Invalid name. A file or folder name can't contain any of the following characters: \\ / : * ? \" < > |");
		            return;
		        }
		
		        try {
		            switch (pathType) {
		                case FOLDER:
		                    folderManager.renameFolder(selectedPath, newName);
				            updateFileList();
		                    break;
		                case FILE:
		                    Path newFilePath = selectedPath.resolveSibling(newName);
		                    FileManager.renameFile(selectedPath, newFilePath);
				            updateFileList();
		                    break;
		                default:
		                    JOptionPane.showMessageDialog(frame, "Selected item is not valid for renaming.");
		            }
		            JOptionPane.showMessageDialog(frame, "Renamed successfully.");
		        } catch (IOException ex) {
		            JOptionPane.showMessageDialog(frame, "Error renaming file or folder: " + ex.getMessage());
		        }
		    }
		});

		deleteItem.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String selectedItem = fileList.getSelectedValue();
		        if (selectedItem != null) {
		            Path selectedPath = folderManager.getCurrentDirectory().resolve(selectedItem);
		            PathType pathType = checkPathType(selectedPath);

		            try {
		                switch (pathType) {
		                    case FOLDER:
		                        folderManager.deleteFolder(selectedPath);
		                        break;
		                    case FILE:
		                        FileManager.deleteFile(selectedPath);
		                        break;
		                    default:
		                        System.out.println("Selected item is not valid for deletion.");
		                        return;
		                }
		                updateFileList();
		            } catch (IOException ex) {
		                System.out.println("Error deleting item: " + ex.getMessage());
		            }
		        } else {
		            System.out.println("No item selected for deletion.");
		        }
		    }
		});



        
        
        
    }

    
    
 
    
}
