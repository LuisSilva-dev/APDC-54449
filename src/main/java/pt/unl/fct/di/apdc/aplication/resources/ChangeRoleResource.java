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

import pt.unl.fct.di.apdc.aplication.util.ChangeRoleData;
import pt.unl.fct.di.apdc.aplication.util.RegisterData;

@Path("/changeRole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeRoleResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ChangeRoleResource() {

	}

	@PUT
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeRole(ChangeRoleData data) {
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
			if(user == null || userModify == null || user.getString("role").equals(RegisterData.USER_BO)
					|| (!data.username.equals(user.getKey().getName())
							&& userModify.getString("role").equals(RegisterData.USER_BO))) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("invalid parameters or users or the user to be modified is a userBackOffice").build();
			}
			user = Entity.newBuilder(user)
					.set("role", RegisterData.USER_BO)
					.set("timestamp", Timestamp.now()).build();
			txn.update(user);
			txn.commit();
			return Response.status(Status.OK).entity("success").build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}
