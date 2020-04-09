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
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.aplication.util.RegisterData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	private final Gson g = new Gson();
	
	public RegisterResource() {
		
	}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistration(RegisterData data) {
		
		LOG.fine("Attempting to register the username: " + data.username);
		
		if(!data.validRegistration())
			return Response.status(Status.BAD_REQUEST).entity("missing or wrong paramenter").build();
		Transaction txn = datastore.newTransaction();
		try {
		Key userKey = datastore.newKeyFactory().setKind("username").newKey(data.username);
		
		if(txn.get(userKey) != null) {
			txn.rollback();
			return Response.status(Status.CONFLICT).entity("invalid username").build();
		}
		Entity person = Entity.newBuilder(userKey)
			.set("email", data.email).set("password", DigestUtils.sha512Hex(data.password))
			.set("confirmation", DigestUtils.sha512Hex(data.confirmation))
			.set("timestamp", Timestamp.now()).set("role", RegisterData.USER)
			.set("perfil", data.perfil).set("telefoneFixo", data.telefoneFixo).set("telefoneMovel", data.telefoneMovel)
			.set("morada", data.morada).set("estado", data.estado).set("ativa", RegisterData.ACTIVE).build();
		txn.put(person);
		txn.commit();
		return Response.ok(g.toJson(person)).build();
		}finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}
