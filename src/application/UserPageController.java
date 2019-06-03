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
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
	
	@FXML Label lblUsername;
	@FXML Button logout, create, delete, upload;
	@FXML ListView<String> lvFiles;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

//		System.out.println(files);
//		lblUsername.setText(MainController.user);	
		lvFiles.setItems(listFiles);
		getFiles();

	}
//	
	@FXML
	public void signOut(ActionEvent event) {

		try {		
			
			((Node)event.getSource()).getScene().getWindow().hide();
			Stage primaryStage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			Parent root = loader.load(getClass().getResource("Main.fxml").openStream());
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
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
				
			  listFiles.addAll(selectedFiles.iterator().next().getName()+"\t\t\t"+ "~YOU");
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
		 }
		} catch (SQLException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			
			e1.printStackTrace();
		}
		

	}
	
	@FXML
	public void onDelete(ActionEvent event) {
		String file = lvFiles.getSelectionModel().getSelectedItem();
		Connection connect = null;
		PreparedStatement statement=null;
		try {
			connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword); 
			statement = connect.prepareStatement("DELETE FROM files where filename=? AND uploaded>0");
			statement.setString(1, file);
			int l = statement.executeUpdate();
			if(l<1) {
				alert.setAlertType(AlertType.ERROR);
				alert.setContentText("cannot delete");
				alert.show();
			}
			else {
				alert.setAlertType(AlertType.INFORMATION);
				alert.setContentText("File deleted");
				alert.show();
			}
			getFiles();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	@FXML
	public void onUpload(ActionEvent event) {
		
		try {
			String file = lvFiles.getSelectionModel().getSelectedItem();
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
	public void getUser(String user, int id) {
		lblUsername.setText("Welcome "+user);
		this.user = user;
		this.userId = id;
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
				listFiles .add(rs.getString("filename"));
			}
//			lvFiles.setItems(listFiles);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

		

}
