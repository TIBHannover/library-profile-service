package eu.tib.profileservice.service;

import java.util.List;

import eu.tib.profileservice.domain.User;

public interface UserService {
	
	public List<User> findAll();
	
	public User findByName(final String name);
	
	public User create(final User user);

}
