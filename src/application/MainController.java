package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
			
			lblStatus.setText(""); //for testing purposes
			
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
