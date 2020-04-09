package pt.unl.fct.di.apdc.aplication.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import pt.unl.fct.di.apdc.aplication.util.LogoutData;
import pt.unl.fct.di.apdc.aplication.util.RegisterData;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LogoutResource() {

	}

	@DELETE
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogout(LogoutData data) {
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
			if(user == null || user.getString("ativa").equals(RegisterData.NOT_ACTIVE)) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("invalid user").build();
			}
			txn.delete(tokenKey);
			txn.commit();
			return Response.status(Status.OK).entity("user logged out").build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}
