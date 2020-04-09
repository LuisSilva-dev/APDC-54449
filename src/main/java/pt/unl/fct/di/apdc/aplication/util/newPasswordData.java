package pt.unl.fct.di.apdc.aplication.util;

public class newPasswordData {
	public String tokenID, password, oldPassword, confirmation;
	public newPasswordData() {
		
	}
	
	public newPasswordData(String tokenID, String password, String confirmation, String oldPassword) {
		this.tokenID = tokenID;
		this.password = password;
		this.confirmation = confirmation;
		this.oldPassword = oldPassword;
	}
}
