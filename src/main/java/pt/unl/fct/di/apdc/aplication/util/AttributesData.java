package pt.unl.fct.di.apdc.aplication.util;

public class AttributesData {
	public String username, perfil, telefoneFixo, telefoneMovel, morada, estado, tokenID;
	
	public AttributesData() {
		
	}
	public AttributesData(String username,
			String perfil, String telefoneFixo, String telefoneMovel, String morada, String estado, String tokenID) {
		this.username = username;
		this.perfil = perfil;
		this.telefoneFixo = telefoneFixo;
		this.telefoneMovel = telefoneMovel;
		this.morada = morada;
		this.estado = estado;
		this.tokenID = tokenID;
	}
}
