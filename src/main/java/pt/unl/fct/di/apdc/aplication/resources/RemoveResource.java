package pt.unl.fct.di.apdc.aplication.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import pt.unl.fct.di.apdc.aplication.util.RegisterData;
import pt.unl.fct.di.apdc.aplication.util.RemoveData;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public RemoveResource() {

	}

	@DELETE
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response remove(RemoveData data) {
		Key tokenKey = datastore.newKeyFactory().setKind("tokenID").newKey(data.tokenID);
		Transaction txn = datastore.newTransaction();
		try {
			Entity token = txn.get(tokenKey);
			if(token == null || Long.parseLong(token.getString("expirationData")) <= System.currentTimeMillis()) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("invalid token login again").build();
			}
			Key key = datastore.newKeyFactory().setKind("username").newKey(token.getString("username"));
			Entity user = txn.get(key);
			Key keyModify = datastore.newKeyFactory().setKind("username").newKey(data.username);
			Entity userModify = txn.get(keyModify);
			if(user == null || user.getString("ativa").equals(RegisterData.NOT_ACTIVE) || userModify == null || !data.username.equals(user.getKey().getName())
					&& user.getString("role").equals(RegisterData.USER)) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("invalid username").build();
			}
			userModify = Entity.newBuilder(userModify).set("ativa", RegisterData.NOT_ACTIVE)
					.set("timestamp", Timestamp.now()).build();
			txn.update(userModify);
			txn.delete(tokenKey);
			txn.commit();
			return Response.status(Status.OK).entity("success").build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}