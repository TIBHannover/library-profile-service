package eu.tib.profileservice.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Transactional
	public List<User> findAll() {
		return userRepository.findAll();
	}
	
	@Transactional
	public User create(final User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	@Transactional
	@Override
	public User update(User user) {
		final User currentUser = findById(user.getId());
		if (currentUser == null) {
			throw new IllegalArgumentException("user does not exist");
		}
		if (!currentUser.getPassword().equals(user.getPassword())) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			LOG.debug("Change password of user {}", user.getName());
		}
		return userRepository.save(user);
	}

	@Override
	public User findByName(final String name) {
		return userRepository.findByName(name);
	}
	
	@Transactional
	@Override
	public void delete(final User user) {
		userRepository.delete(user);
	}

	@Transactional
	@Override
	public User findById(Long id) {
		if (id == null) {
			return null;
		} 
		try {
			return userRepository.findById(id).get();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

}
