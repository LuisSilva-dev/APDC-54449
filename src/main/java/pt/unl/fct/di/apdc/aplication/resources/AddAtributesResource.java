package pt.unl.fct.di.apdc.aplication.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.aplication.util.AttributesData;
import pt.unl.fct.di.apdc.aplication.util.RegisterData;

@Path("/addAttrib")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class AddAtributesResource {
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final Gson g = new Gson();
	
	public AddAtributesResource() {
		
	}
	
	@PUT
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addAttrib(AttributesData data) {
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
			Key keyModify = datastore.newKeyFactory().setKind("username").newKey(data.username);
			Entity userModify = txn.get(keyModify);
			if(user == null || userModify == null || (!data.username.equals(user.getKey().getName())
					&& user.getString("role").equals(RegisterData.USER))
					|| user.getString("ativa").equals(RegisterData.NOT_ACTIVE)) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("invalid username").build();
			}
			userModify = Entity.newBuilder(userModify)
					.set("timestamp", Timestamp.now())
					.set("perfil", data.perfil).set("telefoneFixo", data.telefoneFixo)
					.set("telefoneMovel", data.telefoneMovel)
					.set("morada", data.morada).set("estado", data.estado).build();
			txn.update(userModify);
			txn.commit();
			return Response.status(Status.OK).entity(g.toJson(userModify)).build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}
