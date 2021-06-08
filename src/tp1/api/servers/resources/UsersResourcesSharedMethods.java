package tp1.api.servers.resources;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.discovery.Discovery;
import tp1.api.server.rest.SpreadSheetsServer;
import tp1.api.workers.DeleteSpreadSheetWorker;

public class UsersResourcesSharedMethods {
	private Map<String,User> users;
	//private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	private String spreadServiceId;

	public final Discovery martian;
	private String secrete;
	
	public UsersResourcesSharedMethods(String domainName, Discovery martian,String secrt) {
		users = new HashMap<String, User>();
		this.martian=martian;
		this.secrete=secrt;
		spreadServiceId =domainName+":"+SpreadSheetsServer.SERVICE;
	}

	public String createUser(User user) {
		//Log.info("createUser : " + user);
		validCredentials(user);

		synchronized (users) {
			// Check if user is valid, if not return HTTP CONFLICT (409)
			
			// Check if userId does not exist exists, if not return HTTP CONFLICT (409)
			if( users.containsKey(user.getUserId())) {
				//Log.info("User already exists.");
				throw new WebApplicationException( Status.CONFLICT );
			}

			//Add the user to the map of users
			users.put(user.getUserId(), user);
		}
		
		return user.getUserId();
	}

	private void validCredentials(User user) {
		if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || 
				user.getEmail() == null) {
			throw new WebApplicationException( Status.BAD_REQUEST );
		}
	}
	
	private void userExists(User user) {
		if( user == null ) {
			throw new WebApplicationException( Status.NOT_FOUND );
		}
	}
	
	private void passwordCorrect(User user,String password) {
		if(!user.getPassword().equals(password)) {
			throw new WebApplicationException( Status.FORBIDDEN );
		}
	}
	public User getUser(String userId, String password) {
				
		User user = users.get(userId);
		
		// Check if user exists 
		userExists(user);
		
		//Check if the password is correct
		passwordCorrect(user,password);
				
		return user;
	}

	public User updateUser(String userId, String password, User user) {
		// DID Complete method
		User oldUser = users.get(userId);
		
		
		synchronized (users) {
			userExists(oldUser);
			//Check if the password is correct
			passwordCorrect(oldUser,password);
			if(user.getEmail()!=null) {
			oldUser.setEmail(user.getEmail());
			}
			if(user.getFullName()!=null) {
			oldUser.setFullName(user.getFullName());
			}
			if(user.getPassword()!=null) {
			oldUser.setPassword(user.getPassword());
			}
		}
		return oldUser;
	}

	public User deleteUser(String userId, String password) {
		// TODO Complete method, delete the spreadsheet of this user...
		User user;
		synchronized (users) {
			user= users.get(userId);
			
			userExists(user);
			
			//Check if the password is correct
			passwordCorrect(user,password);
			
			if(users.remove(userId)!=null) {
				//TO-DO-LATER:THINK ABOUT USING THREAD POOLS
				DeleteSpreadSheetWorker theDeleter = new DeleteSpreadSheetWorker(userId,spreadServiceId,martian,secrete);
				theDeleter.start();
			}
			
		}
		

		return user;
	}

	public List<User> searchUsers(String pattern) {
		List<User> results = new LinkedList<User>();		
		try {
			String query=pattern.trim().toLowerCase();
			
			synchronized (users) {
				for (Map.Entry<String,User> entry : this.users.entrySet()) {
					if(entry.getValue().getFullName().toLowerCase().contains(query)) {
						results.add(cloneUser(entry.getValue()));
					}
				}
			}
		}catch (Exception e) {
			throw new WebApplicationException( Status.BAD_REQUEST );
		}		
		return results;
	}
	
	private User cloneUser(User u) {
		return new User(u.getUserId(), u.getFullName(),u.getEmail(),"");
	}
	public User hasThisUser(String userId) {
		return users.get(userId);
	}
}
