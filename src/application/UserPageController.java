package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class UserPageController implements Initializable {
	
	String dbUrl = "jdbc:mysql://localhost:3306/docmanager?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	static String dbuser = "root";
	static String dbpassword ="pass";
	int userId;
	private String user;
	private ObservableList<String> listFiles = FXCollections.observableArrayList();
	Alert alert = new Alert(AlertType.NONE); 
	Alert prompt = new Alert(AlertType.NONE); 
	
	@FXML Label lblUsername;
	@FXML Button logout, create, delete, upload;
	@FXML ListView<String> lvFiles;
	private Stage primaryStage;
	
	
	public UserPageController(User user) throws IOException {
	
		primaryStage = new Stage();
		this.userId = user.getId();
		this.user = user.getUser();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("UserPage.fxml"));
		loader.setController(this);
		Scene scene = new Scene(loader.load());
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setTitle("Document Manager");
		primaryStage.centerOnScreen();
		primaryStage.setScene(scene);
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {

//		System.out.println(files);
//		lblUsername.setText(MainController.user);	
		lblUsername.setText("Welcome "+this.user);
		lvFiles.setItems(listFiles);
		getFiles();
		System.out.println(user+" and "+userId);
		upload.setDisable(true);
		delete.setDisable(true);
		
		lvFiles.setOnMouseClicked(new EventHandler<MouseEvent>() {

		        @Override
		        public void handle(MouseEvent event) {
		    		upload.setDisable(false);
		    		delete.setDisable(false);
		        }
		    });

	}
//	
	@FXML
	public void signOut(ActionEvent event) {
		try {
			MainController mainController = new MainController();
			mainController.showStage();
			primaryStage.hide();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * To select folder from user's window
	 * @param event
	 */
	@FXML
	public void onCreate(ActionEvent event) {
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
				
			  listFiles.addAll(selectedFiles.iterator().next().getName()+"%S~YOU");
			//write to listView
//				lvFiles.setItems(listFiles);			
			 for(File selectedFile : selectedFiles) {

					 //storing file in inputStream to be stored in db
					InputStream inputStream = new FileInputStream(new File(selectedFile.getPath()));
					//store in db
					statement.setString(1, selectedFile.getName());
					statement.setBlob(2, inputStream);
					statement.executeUpdate();
			 }
			 tabCount(listFiles);
		 }
		} catch (SQLException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			
			e1.printStackTrace();
		}
		

	}
	
	@FXML
	public void onDelete(ActionEvent event) {
		
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
				String owner = rs.getString("owner");
				if(!owner.equals(user)) {
					listFiles.add(rs.getString("filename")+"%S~"+owner);
					continue;
				}
				listFiles.add(rs.getString("filename")+"%S~YOU");
//				listFiles.add(rs.getString("filename")+tabCount(listFiles)+"~YOU");	
			}//update list to format spacing
			tabCount(listFiles);
//			lvFiles.setItems(listFiles);
		} catch (SQLException e) {
			
			e.printStackTrace();
		}

	}
	public void showStage() {
		// TODO Auto-generated method stub
		primaryStage.show();
	}
	
	private ArrayList<String> tabCount(ObservableList<String>contents) {
		ArrayList<String>contents2 = new ArrayList<>();
		int longest =0;
		String tabs="\t";
		System.out.println("tab1 "+tabs.length());
		//getting longest
		for(String content : contents) {
			if(longest < content.split("%S")[0].length()) {
				longest = content.trim().length();
			}		
		}
		System.out.println("longes "+longest);
		
		for(String content : contents) {
			int width = (longest-content.trim().split("%S")[0].length())+3;	
			System.out.println("width "+width);
			tabs += String.format( "%"+width+ "s","");
			String text = content.replace("%S", tabs);	
			System.out.println("tab2 "+tabs.length());
			contents2.add(text);
		}
		contents.clear();
		contents.addAll(contents2);
		return contents2;
		
	}

		

}
