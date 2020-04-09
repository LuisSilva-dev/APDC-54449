package pt.unl.fct.di.apdc.aplication.util;

public class RegisterData {
	public static final String USER = "user";
	public static final String USER_BO = "userBO";
	public static final String ACTIVE = "sim";
	public static final String NOT_ACTIVE = "nao";
	public String username, email, password, confirmation, perfil, telefoneFixo, telefoneMovel, morada, estado;
	
	public RegisterData() {
		
	}
	
	public RegisterData(String username, String email, String password, String confirmation,
			String perfil, String telefoneFixo, String telefoneMovel, String morada, String estado) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.confirmation = confirmation;
		this.perfil = perfil;
		this.telefoneFixo = telefoneFixo;
		this.telefoneMovel = telefoneMovel;
		this.morada = morada;
		this.estado = estado;
	}

	private boolean validField(String value) {
		return value != null && !value.equals("");
	}
	public boolean validRegistration() {
		return validField(username) && validField(email) && validField(password)
				&& validField(confirmation) && email.contains("@") && password.equals(confirmation);
	}
}