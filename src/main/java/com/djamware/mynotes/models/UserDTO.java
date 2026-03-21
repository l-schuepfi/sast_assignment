package com.djamware.mynotes.models;

public class UserDTO {

	private String email;

	private String password;

	private String fullname;

	public String getEmail() {
		return email;
	}

	public String getPassword() { return password; }

	public String getFullname() { return fullname; }

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public UserDTO() {
		//needed
	}
}
