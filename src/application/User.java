package application;


public class User {

	 private final String user;
	 private final Integer id;

	public User(String user, Integer id) {
		this.user = user;
		this.id = id;
	}
	
	public String getUser() { 
		return user;
	}
	public Integer getId() {
		return id;
	}
	
}
