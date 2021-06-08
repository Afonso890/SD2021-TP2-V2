package tp1.api.service.soap;

import java.util.List;
import java.util.logging.Logger;

import jakarta.jws.WebService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.discovery.Discovery;
import tp1.api.servers.resources.UsersResourcesSharedMethods;

@WebService(serviceName=SoapUsers.NAME, 
targetNamespace=SoapUsers.NAMESPACE, 
endpointInterface=SoapUsers.INTERFACE)
public class UsersWS implements SoapUsers {

	private static Logger Log = Logger.getLogger(UsersWS.class.getName());
	private final UsersResourcesSharedMethods resoures;

	public UsersWS(String domainName, Discovery martian,String secrete) {
		resoures = new UsersResourcesSharedMethods(domainName, martian,secrete);
	}
	@Override
	public String createUser(User user) throws UsersException {	
		try {
			return resoures.createUser(user);
		}catch(WebApplicationException e) {
			int status = e.getResponse().getStatus();
			String msg;
			if(status==Status.CONFLICT.getStatusCode()) {
				msg="User already exists.";
			}else {
				msg="User object invalid.";
			}
			throw new UsersException(msg);
		}
	}

	@Override
	public User getUser(String userId, String password) throws UsersException {
		Log.info("getUser : user = " + userId + "; pwd = " + password);		
		try {
			return resoures.getUser(userId,password);
		}catch(WebApplicationException e) {
			int status = e.getResponse().getStatus();
			String msg;
			if(status==Status.NOT_FOUND.getStatusCode()) {
				msg="User does not exist.";
			}else {
				msg="Password is incorrect.";
			}
			throw new UsersException(msg);
		}
	}

	@Override
	public User updateUser(String userId, String password, User user) throws UsersException {
		try {
			return resoures.updateUser(userId, password, user);
		}catch(WebApplicationException e) {
			int status = e.getResponse().getStatus();
			String msg;
			if(status==Status.NOT_FOUND.getStatusCode()) {
				msg="User does not exist.";
			}else {
				msg="Password is incorrect.";
			}
			throw new UsersException(msg);
		}
	}

	@Override
	public User deleteUser(String userId, String password) throws UsersException {
		try {
			System.out.println("GOING TO DELETE USER -> "+userId);
			User u = resoures.deleteUser(userId, password);
			System.out.println("USER DELETED");
			return u;
		}catch(WebApplicationException e) {
			int status = e.getResponse().getStatus();
			String msg;
			if(status==Status.NOT_FOUND.getStatusCode()) {
				msg="User does not exist.";
			}else {
				msg="Password is incorrect.";
			}
			throw new UsersException(msg);
		}
	}

	@Override
	public List<User> searchUsers(String pattern) throws UsersException {
		try {
			return resoures.searchUsers(pattern);
		}catch(WebApplicationException e) {
			throw new UsersException("VERY, VERY BAD REQUEST!");
		}
	}
	public User hasThisUser(String userId) {
		System.out.println("ASKING FOOR THIS NAD THIS USER ONLY!!1" + userId);
		User u = resoures.hasThisUser(userId);
		System.out.println(" got -> "+u);
		return u;
	}
}
