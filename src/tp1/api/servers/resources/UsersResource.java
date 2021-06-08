package tp1.api.servers.resources;
import java.util.List;

import jakarta.inject.Singleton;
import tp1.api.User;
import tp1.api.discovery.Discovery;
import tp1.api.service.rest.RestUsers;

@Singleton
public class UsersResource implements RestUsers {
	private final UsersResourcesSharedMethods resourceAux;
	public UsersResource(String domainName, Discovery martian, String secrete) {
		resourceAux = new UsersResourcesSharedMethods(domainName, martian,secrete);
	}

	@Override
	public String createUser(User user) {
		return resourceAux.createUser(user);
	}

	@Override
	public User getUser(String userId, String password) {
		return resourceAux.getUser(userId, password);
	}


	@Override
	public User updateUser(String userId, String password, User user) {
		return resourceAux.updateUser(userId, password, user);
	}


	@Override
	public User deleteUser(String userId, String password) {
		return resourceAux.deleteUser(userId, password);
	}


	@Override
	public List<User> searchUsers(String pattern) {
		return resourceAux.searchUsers(pattern);
	}

	@Override
	public User hasThisUser(String userId) {
		return resourceAux.hasThisUser(userId);
	}
}
