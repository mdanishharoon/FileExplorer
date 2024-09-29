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
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.DefaultListModel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

import java.util.regex.Pattern;



public class fileExplorer {

    private JFrame frame;
    private JTextField pathField;
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private FolderManager folderManager;
    

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


    
    
    public fileExplorer() {
        folderManager = new FolderManager(Paths.get(System.getProperty("user.dir"))); // Initialize with the current working directory
        initialize();
    }

    
    //HELPER FUNCTIONS FOR VALIDATION
    private boolean isValidWindowsName(String name) {
        String invalidChars = "[<>:\"/\\\\|?*]";
        return !Pattern.compile(invalidChars).matcher(name).find();
    }
    
    private void updateFileList() {
        listModel.clear(); 
        List<String> contents = folderManager.listDirectoryContents(); 
        for (String item : contents) {
            listModel.addElement(item); 
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
    
    
    //GUI WRAPPER FUNCTIONS
    private void moveFile() {
        String selectedItem = (String) fileList.getSelectedValue();
        
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(frame, "Please select a file to move.");
            return;
        }

        Path selectedPath = Paths.get(folderManager.getCurrentDirectory().toString(), selectedItem);
        
        if (!Files.isRegularFile(selectedPath)) {
            JOptionPane.showMessageDialog(frame, "You can only move files, not folders.");
            return;
        }

        String destinationPathStr = JOptionPane.showInputDialog(frame, "Enter the destination folder path:");
        
        if (destinationPathStr == null || destinationPathStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Destination path cannot be empty.");
            return;
        }

        Path destinationFolderPath = Paths.get(destinationPathStr);

        if (!Files.exists(destinationFolderPath)) {
            JOptionPane.showMessageDialog(frame, "The destination folder does not exist.");
            return;
        }
        if (!Files.isDirectory(destinationFolderPath)) {
            JOptionPane.showMessageDialog(frame, "The destination must be a valid folder.");
            return;
        }

        Path destinationFilePath = destinationFolderPath.resolve(selectedPath.getFileName());

        int result = JOptionPane.showConfirmDialog(frame, 
            "Are you sure you want to move the file to: " + destinationFilePath.toString(), 
            "Confirm Move", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
			FileManager.moveFile(selectedPath, destinationFilePath);
			updateFileList(); 

			JOptionPane.showMessageDialog(frame, "File moved successfully to " + destinationFolderPath.toString());
        }
    }

    private void getProperties() {
        String selectedItem = (String) fileList.getSelectedValue();
        
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(frame, "Please select a file to view properties.");
            return;
        }

        Path selectedPath = Paths.get(folderManager.getCurrentDirectory().toString(), selectedItem);
        
        if (!Files.isRegularFile(selectedPath)) {
            JOptionPane.showMessageDialog(frame, "You can only view properties for files, not folders.");
            return;
        }

		List<String> properties = FileManager.getFileProperties(selectedPath);
		
		StringBuilder propertyDisplay = new StringBuilder("File Properties:\n");
		for (String property : properties) {
		    propertyDisplay.append(property).append("\n");
		}

		JOptionPane.showMessageDialog(frame, propertyDisplay.toString(), "File Properties", JOptionPane.INFORMATION_MESSAGE);
    }

    private void rename() {
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
    
    private void delete() {
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
    
	private void changeDirectory() {
		String pathInput = pathField.getText().trim();
        Path newPath = Paths.get(pathInput);

        PathType pathType = checkPathType(newPath);

        switch (pathType) {
            case FOLDER:
                try {
                    folderManager.changeDirectory(newPath);
                    updateFileList(); 
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
        pathField.setBounds(155, 24, 498, 25);
        frame.getContentPane().add(pathField);
        pathField.setColumns(10);

        JLabel lblNewLabel = new JLabel("Path");
        lblNewLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        lblNewLabel.setBounds(100, 24, 56, 25);
        frame.getContentPane().add(lblNewLabel);

        JButton btnGo = new JButton("Go");
        btnGo.setBorder(UIManager.getBorder("Button.border"));
        btnGo.setFont(new Font("Consolas", Font.PLAIN, 16));
        btnGo.setBounds(688, 24, 66, 27);
        frame.getContentPane().add(btnGo);

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setFont(new Font("Consolas", Font.PLAIN, 16));
        fileList.setBounds(0, 24, 719, 424);
        frame.getContentPane().add(fileList);

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setBounds(35, 61, 719, 424);
        frame.getContentPane().add(scrollPane); 
        
        JButton backBtn = new JButton("<");
        backBtn.setBounds(35, 23, 55, 25);
        frame.getContentPane().add(backBtn);
        
        

        
        // Setup menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Consolas", Font.PLAIN, 16));
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem renameItem = new JMenuItem("Rename");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem moveItem = new JMenuItem("Move File");
        JMenuItem propertiesItem = new JMenuItem("Properties");
        fileMenu.add(renameItem);
        fileMenu.add(deleteItem);
        fileMenu.add(moveItem);
        fileMenu.add(propertiesItem);


        
        //EVENT HANDLERS
        
        moveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveFile();
            }
        });
        
        propertiesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getProperties();
            }
        });
        
        
		renameItem.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        rename();
		    }

			});

		deleteItem.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        delete();
		    }
		});

		btnGo.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        changeDirectory();
		    }

		});
		
		fileList.addMouseListener((MouseListener) new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 2) {
		            String selectedItem = (String) fileList.getSelectedValue();
		            
		            if (selectedItem == null) {
		                JOptionPane.showMessageDialog(frame, "Please select a file or folder.");
		                return;
		            }

		            Path selectedPath = Paths.get(folderManager.getCurrentDirectory().toString(), selectedItem);
		            
		            if (Files.isDirectory(selectedPath)) {
		                folderManager.changeDirectory(selectedPath);
		                updateFileList(); 
		            } else if (Files.isRegularFile(selectedPath)) {
						FileManager.openFile(selectedPath);
		            } else {
		                JOptionPane.showMessageDialog(frame, "The selected item is not valid.");
		            }
		        }
		    }
		});

		backBtn.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        Path currentDirectory = folderManager.getCurrentDirectory();

		        Path parentDirectory = currentDirectory.getParent();
		        if (parentDirectory != null) {
		            folderManager.changeDirectory(parentDirectory);
		            updateFileList();
		        } else {
		            JOptionPane.showMessageDialog(frame, "You're already at the root directory.");
		        }
		    }
		});




        
        
        
    }
}
