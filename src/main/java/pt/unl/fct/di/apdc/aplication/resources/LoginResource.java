package pt.unl.fct.di.apdc.aplication.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.aplication.util.AuthToken;
import pt.unl.fct.di.apdc.aplication.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final Gson g = new Gson();

	public LoginResource() {

	}

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);
		Key key = datastore.newKeyFactory().setKind("username").newKey(data.username);
		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(key);
			if(user == null || !user.getString("password").equals(DigestUtils.sha512Hex(data.password))) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Incorrect username or password").build();
			}
			if(user.getString("estado").equals("inactive")) {
				txn.rollback();
				Response.status(Status.NOT_ACCEPTABLE).entity("The account is inactive contact an admin in order to get it back").build();
			}
			AuthToken at = new AuthToken(data.username);
			Key tokenKey = datastore.newKeyFactory().setKind("tokenID").newKey(at.tokenID);
			Entity atEntity = Entity.newBuilder(tokenKey).set("username", at.username).set("creationData", at.creationData).set("expirationData", at.expirationData).build();
			txn.put(atEntity);
			txn.commit();
			return Response.ok(g.toJson(at)).build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}
