package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainController {

	private final Stage primaryStage;
	String dbUrl = "jdbc:mysql://localhost:3306/docmanager?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	static String dbuser = "root";
	static String dbpassword ="pass";
	int userId;
	
	@FXML
	TextField txtUsername,txtPassword ;
	@FXML
	Label lblStatus, lblError;
	static String user ;
	public MainController() throws IOException {
		primaryStage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
		loader.setController(this);
		Scene scene = new Scene(loader.load());
		primaryStage.setTitle("Document Manager -signin");
		primaryStage.centerOnScreen();
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
	}

	@FXML
	private void onLogin(ActionEvent e){
		
		lblStatus.setText("");
		String username = txtUsername.getText();
		String password = txtPassword.getText();
		
		if(!username.isEmpty() && !password.isEmpty()) {
			
			if(username.length()>=4 && username.length()>=4) {
				
				if(userExist(username, password)) {
					try {
						UserPageController userPageController = new UserPageController(new User(username,userId));
						userPageController.showStage();
						primaryStage.hide();
					} catch(Exception e1) {
						e1.printStackTrace();
					}
					
				}else lblStatus.setText("user not found");
				
			}else lblStatus.setText("Username and password must be 4 or more charcters long");
				
			}else lblStatus.setText("field cannot be empty");	
	}
	
	@FXML
	public void onCreate() {
		String username = txtUsername.getText();
		String password = txtPassword.getText();
		Alert alert = new Alert(AlertType.NONE);
		lblStatus.setText("");
		try {
			Connection connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			PreparedStatement statement = connect.prepareStatement("INSERT INTO user(username, password) VALUES(?,?)");
			statement.setString(1, username);
			statement.setString(2, password);
			int result = statement.executeUpdate();
			
			lblStatus.setText("user Created. Login to continue");
			txtUsername.clear();
			txtPassword.clear();
			alert.setAlertType(AlertType.INFORMATION);
			alert.setContentText("user Created\nLogin to continue");
			alert.setTitle("Account info");
			alert.setHeaderText("Account Created");
			alert.show();
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			if(e.getMessage().equals("Duplicate entry 'janeDoe' for key 'PRIMARY'")) {
				lblStatus.setText("username exist try another username");
				txtUsername.clear();
				txtPassword.clear();
			}else {
				lblError.setText("Cannot connect to database");
			}
		}
		
	}
	
	/**
	 * Return true if user exist.
	 * @param username
	 * @param accountType
	 * @param password
	 * @return Boolean
	 */
	private Boolean userExist(String username, String password) {
		try {
			//connect to database and prepstatement
			Connection connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			PreparedStatement statement = connect.prepareStatement("SELECT * FROM user WHERE username=? AND password=?");
			statement.setString(1, username);
			statement.setString(2, password);
			ResultSet result = statement.executeQuery();
			
			lblStatus.setText("");
			
			//stores username and password from database
			String passwordString = null;
			String userNameString = null;
			
			//loop through database result
			while(result.next()) {
				//stores name and password from database
				userNameString = result.getString("username");
				passwordString = result.getString("password");
				user =  result.getString("username");
				userId = result.getInt("id");
				//verify name and password equals any account on the db
				if(userNameString.equals(username) && passwordString.equals(password)) return true;
					
			}			
			
		} catch (SQLException e) {
			e.printStackTrace();
			lblError.setText("Cannot connect to database");
			return null;
		}
		return false;
	}

	public void showStage() {

		primaryStage.show();
	}
}
