package pt.unl.fct.di.apdc.aplication.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.aplication.util.RegisterData;
import pt.unl.fct.di.apdc.aplication.util.newPasswordData;

@Path("/newPassword")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class newPasswordResource {
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	private final Gson g = new Gson();
	
	public newPasswordResource() {
		
	}
	
	@PUT
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doNewPassword(newPasswordData data) {
		Key tokenKey = datastore.newKeyFactory().setKind("tokenID").newKey(data.tokenID);
		Transaction txn = datastore.newTransaction();
		try {
		Entity token = txn.get(tokenKey);
		if(token == null || Long.parseLong(token.getString("expirationData")) <= System.currentTimeMillis()) {
			txn.rollback();
			return Response.status(Status.FORBIDDEN).entity("invalid token, login again").build();
		}
		Key key = datastore.newKeyFactory().setKind("username").newKey(token.getString("username"));
		Entity user = txn.get(key);
		if(user == null || user.getString("ativa").equals(RegisterData.NOT_ACTIVE)
				|| data.oldPassword.equals(data.password)
				|| !user.getString("password").equals(DigestUtils.sha512Hex(data.oldPassword)) || !data.password.equals(data.confirmation)) {
			txn.rollback();
			return Response.status(Status.FORBIDDEN).entity("the password and confirmation do not match or the user is invalid or the new password is the same as the old").build();
		}
		user = Entity.newBuilder(user)
				.set("password", DigestUtils.sha512Hex(data.password))
				.set("timestamp", Timestamp.now()).build();
		txn.update(user);
		txn.commit();
		return Response.status(Status.OK).entity(g.toJson(user)).build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}
