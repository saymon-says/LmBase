package com.example.lmbase.Model;

public class Users {
	String alias, fullname, userpic;

	public Users() {
	}

	public Users(String alias, String fullname, String userpic) {
		this.alias = alias;
		this.fullname = fullname;
		this.userpic = userpic;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getUserpic() {
		return userpic;
	}

	public void setUserpic(String userpic) {
		this.userpic = userpic;
	}
}
