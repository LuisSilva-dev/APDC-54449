package pt.unl.fct.di.apdc.aplication.util;

public class ChangeRoleData {
	public String tokenID, username;
	public ChangeRoleData() {
		
	}
	
	public ChangeRoleData(String username, String tokenID) {
		this.username = username;
		this.tokenID = tokenID;
	}
}
