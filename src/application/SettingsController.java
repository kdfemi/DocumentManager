package application;

import java.io.IOException;
import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsController {

	private final Stage primaryStage;
	private final Preferences preferences;
	
	@FXML
	TextField txtIp, txtPort, txtUsername;
	@FXML
	PasswordField txtPassword;
	MainController mainController;
	public SettingsController(MainController mainController) throws IOException {	
		
		this.mainController = mainController;
		preferences = Preferences.userRoot().node("DocumentManager");
		
		primaryStage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"));
		loader.setController(this);
		Scene scene = new Scene(loader.load());
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.setX(260);
		
	}
	public void showStage() {
		primaryStage.initModality(Modality.APPLICATION_MODAL);
		primaryStage.show();
	}
	@FXML
	public void onCancel() {
		primaryStage.close();
	}
	@FXML
	public void onSave() {
		Alert a = new Alert(AlertType.CONFIRMATION);
		ButtonType accept = new ButtonType("Accept");
		ButtonType cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
		a.getButtonTypes().setAll(accept,cancel);
		Optional<ButtonType> option =a.showAndWait();
		if(option.get() == accept ) {
			if(txtIp.getText().isEmpty() || txtPort.getText().isEmpty() || txtUsername.getText().isEmpty()||txtPassword.getText().isEmpty()) {
				a = new Alert(AlertType.ERROR);
				a.setContentText("Input fields cannot be empty");
				a.show();
				return;
			}else {
				preferences.put("ip", txtIp.getText());
				preferences.put("port",txtPort.getText());
				preferences.put("username",txtUsername.getText());
				preferences.put("password",txtPassword.getText());
				a= new Alert (AlertType.INFORMATION);
				a.setContentText("Done");
				a.showAndWait();
				mainController.lblError.setText("");
				mainController.setSettings();
				primaryStage.close();
			}
		}
		primaryStage.close();
	}
	
	@FXML
	public void initialize() {
		// TODO Auto-generated method stub
		txtIp.setText(preferences.get("ip","10.152.2.39"));
		txtPort.setText(preferences.get("port","3306"));
		txtUsername.setText(preferences.get("username","user"));
		txtPassword.setText(preferences.get("password","pass"));
	}
	
}
