package application;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainController {

	private final Stage primaryStage;
	private final Preferences preferences;
	private final String dbUrl;
	private final String dbuser ;
	private final String dbpassword ;
	private final Connection connect;
	int userId; //checking something
	
	@FXML
	TextField txtUsername,txtPassword ;
	@FXML
	Label lblStatus, lblError;
	static String user ;
	public MainController() throws IOException, SQLException {
		
		preferences = Preferences.userRoot().node("DocumentManager");
		this.dbpassword = preferences.get("password","pass");
		this.dbuser = preferences.get("username","user");
		this.dbUrl = "jdbc:mysql://"+preferences.get("ip","10.152.2.39")+":"+preferences.get("port","3306")+"/docmanager?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
		this.connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
		primaryStage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
		System.out.println(getClass().getName()+"check" );
		loader.setController(this);
		Scene scene = new Scene(loader.load());
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setTitle("Document Manager -signin");
		primaryStage.centerOnScreen();
		primaryStage.setResizable(false);
		primaryStage.setScene(scene);
		
	} 
	@FXML
	public void initialize(){
		System.out.println("Worked");
		StringBuilder sb = new StringBuilder();
		InputStream stream = this.getClass().getResourceAsStream("sqlQuery.txt");
		String so ="";
		try {
		
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			Connection connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			Statement statement = connect.createStatement();
			while((so=br.readLine()) != null) {
				sb.append(so);
			}
			String[] queries = sb.toString().split(";");
			for(String query : queries) {
				if(!query.isEmpty()) {
					statement.executeUpdate(query);
				}
			}	 
			 
		} catch (IOException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
						lblStatus.setText("user found");
						UserPageController userPageController = new UserPageController(new User(username,userId));
						userPageController.showStage();
						primaryStage.hide();
					} catch(Exception e1) {
						e1.printStackTrace();
					}
					
				}else lblStatus.setText("user not found");
				
			}else lblStatus.setText("Username and password must be 4 or more charcters long");
				
			}else lblStatus.setText("fields cannot be empty");	
	}
	
	@FXML
	public void onCreate() {
		String username = txtUsername.getText();
		String password = txtPassword.getText();
		Alert alert = new Alert(AlertType.NONE);
		lblStatus.setText(""); 
		try {
			if(!username.isEmpty() && !password.isEmpty()) {
				
				if(username.length()>=4 && username.length()>=4) {
					
//					Connection connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
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
				}else lblStatus.setText("Username and password must be 4 or more charcters long");
					
				}else lblStatus.setText("fields cannot be empty");	

				
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
	
	@FXML
	public void onSettings() throws IOException {
		SettingsController settingsController = new SettingsController(this);
		settingsController.showStage();
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
