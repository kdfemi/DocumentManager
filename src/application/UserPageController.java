package application;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class UserPageController implements Initializable {
	
//	String dbUrl = "jdbc:mysql://10.152.2.39:3306/docmanager?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	private final Preferences preferences;
	private final String dbUrl;
	private final String dbuser;
	private final String dbpassword; 
	int userId;
	private String user;
	private ObservableList<String> listFiles = FXCollections.observableArrayList();
	Alert alert = new Alert(AlertType.NONE); 
	Alert prompt = new Alert(AlertType.NONE);  
	List <File> tempList = new ArrayList<>();
	@FXML Label lblUsername;
	@FXML Button logout, create, delete, upload;
	@FXML ListView<String> lvFiles;
	@FXML CheckBox cbDeleteFile;
	private Stage primaryStage;
	private Scene scene;
	 
	public UserPageController(User user)  {
		preferences = Preferences.userRoot().node("DocumentManager");
		this.dbpassword = preferences.get("password","pass");
		this.dbuser = preferences.get("username","user");
		this.dbUrl = "jdbc:mysql://"+preferences.get("ip","10.152.2.39")+":"+preferences.get("port","3306")+"/docmanager?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
		
	try {	
		primaryStage = new Stage();
		this.userId = user.getId();
		this.user = user.getUser();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("userPage.fxml"));
		loader.setController(this);
		this.scene = new Scene(loader.load());
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		scene.setOnMousePressed(e->{
			if(!lvFiles.equals(e.getSource())) {
				lvFiles.getParent().requestFocus();
				disableButtons();
			}
		});
		primaryStage.setScene(scene); 
		 //why not working
		primaryStage.setTitle("Document Manager");
		primaryStage.centerOnScreen();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		

	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {

//		System.out.println(files);
//		lblUsername.setText(MainController.user);	
		lblUsername.setText("Welcome "+this.user);
		lvFiles.setItems(listFiles);
		getFiles();
		cbDeleteFile.setSelected(preferences.getBoolean("deleteFileAfter",false));
		System.out.println(user+" and "+userId);
		disableButtons();

		lvFiles.setOnMouseClicked(new EventHandler<MouseEvent>() {

		        @Override
		        public void handle(MouseEvent event) {

		    		upload.setDisable(false);
		    		delete.setDisable(false);
		    		if(event.getClickCount()>1) {
		    			FileOutputStream fileStream = null;
						try {
							String filename = lvFiles.getSelectionModel().getSelectedItem().split("\t")[0];
//							String owner = lvFiles.getSelectionModel().getSelectedItem().split("\t")[1].trim().split("~")[1].toLowerCase();
//							if(owner.equals("you")) owner = user;
//							System.out.println(owner);
							Connection connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
							PreparedStatement statement = connect.prepareStatement("SELECT * FROM files where filename=?");
							statement.setString(1, filename);
//							statement.setString(2, owner);
							ResultSet rs  = statement.executeQuery();
							File myFile=null;
							//temp files
							if(cbDeleteFile.isSelected()) {
								myFile = File.createTempFile("temp~",filename);
								myFile.deleteOnExit(); 
								Path path = Paths.get(myFile.getAbsolutePath());
								Files.setAttribute(path, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
								tempList.add(myFile);
								}else myFile = new File(filename);
							getNewName(myFile);
							fileStream = new FileOutputStream(myFile);
							
		    			while(rs.next()) {
		    				InputStream theFile = rs.getBinaryStream("file");
		    				byte [] buffer = new byte[1024];
		    				while(theFile.read(buffer)>0) {
		    					fileStream.write(buffer);
		    	
		    				}
		    				if (Desktop.isDesktopSupported()) {
		    				    try {
		    				       
		    				        Desktop.getDesktop().open(myFile);
		    				    } catch (IOException ex) {
		    				        // no application registered for PDFs
		    				    }
		    				}
		    			}
						} catch (SQLException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally {
							try {
								fileStream.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
		    		
		    		}
		        }
		    });
	}
//	
	@FXML
	public void signOut(ActionEvent event) throws SQLException {
		try {
			for(File file:tempList) {
				file.delete();
			}
			MainController mainController = new MainController();
			mainController.showStage();
			primaryStage.hide();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@FXML
	public void listUser(ActionEvent event) {
		disableButtons();
		Alert alert = new Alert(AlertType.NONE);
		String userList = "";
		try{
			Connection connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			Statement statement = connect.createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM user");
			int index = 1;
			while(result.next()) {
				userList += index+" "+result.getString("username")+"\n";
				index++;
			}
		}catch(SQLException e) {
			
		}
		alert.setContentText(userList);
		ButtonType cancel = new ButtonType("Close", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().add(cancel);
		System.out.println("clicked");
		alert.show();
	}
	
	/**
	 * To select folder from user's window
	 * @param event
	 */
	@FXML
	public void onCreate(ActionEvent event) {
		((Button)event.getTarget()).requestFocus();
		 FileChooser fc = new FileChooser();
		 fc.setInitialDirectory(new File(System.getProperty("user.home")+"/desktop"));
		 fc.getExtensionFilters().addAll(new ExtensionFilter("Documents","*.pdf","*.doc","*.docx","*.txt","*.xls","*.ppt"));
		List<File> selectedFiles  = fc.showOpenMultipleDialog(null);
		Connection connect = null;
		PreparedStatement statement=null;
		try {
			connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
		 statement = connect.prepareStatement("INSERT INTO files(filename, file, uploaderId, owner) VALUES(?,?,?,?)");
		 //set the id of the file creator 
		 statement.setInt(3, userId);
		 statement.setString(4, user);
		 if(selectedFiles !=null) {
			 
				//write to listView
			  listFiles.addAll(selectedFiles.iterator().next().getName());
			
//				lvFiles.setItems(listFiles);			
			 for(File selectedFile : selectedFiles) {

					 //storing file in inputStream to be stored in db
					InputStream inputStream = new FileInputStream(new File(selectedFile.getPath()));
					//store in db
					statement.setString(1, selectedFile.getName());
					statement.setBlob(2, inputStream);
					statement.executeUpdate();
			 }
//			 tabCount(listFiles);
		 }
		} catch (SQLException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			
			e1.printStackTrace();
		}
		
	}
	
	@FXML
	public void onDelete(ActionEvent event) {
		((Button)event.getTarget()).requestFocus();
		String file = lvFiles.getSelectionModel().getSelectedItem().split("\t")[0];
		Connection connect = null;
		PreparedStatement statement=null;
		try {
			connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		//prompt if user want to totally delete all delete from all
		prompt.setAlertType(AlertType.CONFIRMATION);
		prompt.setHeaderText("Type of Delete");
		prompt.setTitle("Delete");
		ButtonType hide = new ButtonType("only visible to you");
		ButtonType delete = new ButtonType("delete completely");
		ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		prompt.getButtonTypes().setAll(hide, delete, cancel);
		Optional<ButtonType> result = prompt.showAndWait();
		if (result.get() == hide){
			 try {
				 System.out.println(user+" and "+userId+" "+file);
				statement = connect.prepareStatement("UPDATE files SET uploaded=0 where uploaderId=? AND filename=? ");
				statement.setInt(1, userId);
				statement.setString(2, file);
				int done = statement.executeUpdate();
				if(done<1) {
					alert.setAlertType(AlertType.ERROR);
					alert.setContentText("The file is not uploaded yet or not uploaded by you ");
					alert.show();
				}
				else {
					alert.setAlertType(AlertType.INFORMATION);
					alert.setContentText("File hidden from others");
					alert.show();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(result.get() == delete){
			try {
				
				statement = connect.prepareStatement("DELETE FROM files where filename=? AND  uploaderId=?");
				statement.setString(1, file);
				statement.setInt(2, userId);
				int l = statement.executeUpdate();
				if(l<1) {
					alert.setAlertType(AlertType.ERROR);
					alert.setContentText("cannot delete because file is not uploaded yet or not uploaded by you");
					alert.show();
				}
				else {
					alert.setAlertType(AlertType.INFORMATION);
					alert.setContentText("File deleted from database");
					alert.show();
				}
				getFiles();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
	
	@FXML
	public void onUpload(ActionEvent event) {
		((Button)event.getTarget()).requestFocus();
		try {
			String file = lvFiles.getSelectionModel().getSelectedItem().split("\t")[0];
			Connection connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			PreparedStatement statement = connect.prepareStatement("UPDATE files SET uploaded=1 where uploaderId=? AND filename=? ");
			statement.setInt(1, userId);
			statement.setString(2, file);
			int done = statement.executeUpdate();
			if(done<1) {
				alert.setAlertType(AlertType.ERROR);
				alert.setContentText("Not done");
				alert.show();
			}
			else {
				alert.setAlertType(AlertType.INFORMATION);
				alert.setContentText("Done");
				alert.show();
			}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	
	@FXML
	public void onCheck(ActionEvent e) {
		if(cbDeleteFile.isSelected()) {
			preferences.putBoolean("deleteFileAfter",true);
		}else {preferences.putBoolean("deleteFileAfter",false);}
	}
	public void getUser() {
		lblUsername.setText("Welcome "+this.user);
	}
	
	public void getFiles() {
		try {
			//connect to database and prepstatement collect all files and set it to a table
			listFiles.clear();
			Connection connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			PreparedStatement statement = connect.prepareStatement("SELECT * FROM files where uploaderId=? OR uploaded > 0");
			statement.setInt(1, userId);
			ResultSet rs  = statement.executeQuery();
			while(rs.next()) {
//				String owner = rs.getString("owner");
//				if(!owner.equals(user)) {
//					listFiles.add(rs.getString("filename")+"%S~"+owner);
//					continue;
//				}
				listFiles.add(rs.getString("filename"));
//				listFiles.add(rs.getString("filename")+tabCount(listFiles)+"~YOU");	
			}//update list to format spacing
//			tabCount(listFiles);
//			lvFiles.setItems(listFiles);
		} catch (SQLException e) {
			
			e.printStackTrace();
		}

	}
	public void showStage() {
		// TODO Auto-generated method stub
		primaryStage.show();
	}
	
public void disableButtons() {
	delete.setDisable(true); 
	upload.setDisable(true);
}
		

String getNewName(File file) {
	String filename="";
	final Pattern PATTERN = Pattern.compile("(.*?)(?:\\((\\d+)\\))?(\\.[^.]*)?");
    if (file.exists()) {
        Matcher m = PATTERN.matcher(file.getName());
        if (m.matches()) {
            String prefix = m.group(1);
            String last = m.group(2);
            String suffix = m.group(3);
            if (suffix == null) suffix = "";

            int count = last != null ? Integer.parseInt(last) : 0;

            do {
                count++;
                filename = prefix + "(" + count + ")" + suffix;
                file.renameTo(new File(filename));
            } while (file.exists());
        }
    }
    return filename;
}
}
