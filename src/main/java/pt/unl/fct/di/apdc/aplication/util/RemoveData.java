package pt.unl.fct.di.apdc.aplication.util;

public class RemoveData {
	public String username, tokenID;
	public RemoveData() {
		
	}
	
	public RemoveData(String username, String role, String tokenID) {
		this.username = username;
		this.tokenID = tokenID;
	}
}
