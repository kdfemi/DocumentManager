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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
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
	@FXML Label lblUsername;
	@FXML Button logout, create, delete, upload;
	@FXML TableView <Files>table;
	@FXML private TableColumn<Files, File>fileName;
	@FXML private TableColumn<Files, Boolean> status;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
//		table.getSelectionModel().getSelected
		table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				// TODO Auto-generated method stub
		           TableViewSelectionModel<Files> selectionModel = table.getSelectionModel();
		           ObservableList<?> selectedCells = selectionModel.getSelectedCells();
		           TablePosition<Object, ?> tablePosition = (TablePosition<Object, ?>) selectedCells.get(0);
		           Object val = tablePosition.getTableColumn().getCellData(newValue);
		           Object val2 = tablePosition.getTableColumn().getCellData(newValue);
		           
		          
		           System.out.println("Selected Value " + val);
			}
			
		});
//		System.out.println(files);
//		lblUsername.setText(MainController.user);	
		try {
			//connect to database and prepstatement collect all files and set it to a table
			Connection connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			PreparedStatement statement = connect.prepareStatement("SELECT * FROM user where id=?");
			statement.setInt(1, userId);
			ResultSet rs  = statement.executeQuery();
			
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
		List<File> selectedFile  = fc.showOpenMultipleDialog(null);
		Connection connect = null;
		PreparedStatement statement=null;
		try {
			connect = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
		 statement = connect.prepareStatement("SELECT * FROM user where id=?");
		 statement.setInt(1, userId);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		fileName.setCellValueFactory(new PropertyValueFactory<Files, File>("name"));
		status.setCellValueFactory(new PropertyValueFactory<Files, Boolean>("status"));
		
		 if(selectedFile !=null) {
			 for(int i =0; i< selectedFile.size(); i++) {
				 table.getItems().addAll(new Files(selectedFile.get(i).getAbsoluteFile(), false));
				 try {
					InputStream inputStream = new FileInputStream(new File(selectedFile.get(i).getPath()));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }	
		 }
	}
	
	@FXML
	public void onDelete(ActionEvent event) {
		
	}
	
	@FXML
	public void onUpload(ActionEvent event) {
		
	}
	public void getUser(String user, int id) {
		lblUsername.setText("Welcome "+user);
		this.user = user;
	}

		

}
