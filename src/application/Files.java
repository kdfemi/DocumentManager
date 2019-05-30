package application;

import java.io.File;

import javafx.beans.property.SimpleStringProperty;

public class Files {

	private File fileName;
	private boolean status;
	
	public Files(File file, boolean status) {
		super();
		this.fileName = file;
		this.status = status;
	}
	
	public File getName() {
		return fileName;
	}
	
	public boolean getStatus() {
		return status;
	}

	
}
