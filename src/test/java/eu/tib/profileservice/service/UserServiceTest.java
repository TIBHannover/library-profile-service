package eu.tib.profileservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.repository.UserRepository;

@RunWith(SpringRunner.class)
public class UserServiceTest {
	
    @TestConfiguration
    static class TestContextConfiguration {
          @Bean
        public UserService service() {
            return new UserServiceImpl();
        }
    }

    @Autowired
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    
	private User newUser(final String name, final String password, final String initials) {
		final User user = new User();
		user.setName(name);
		user.setPassword(password);
		user.setInitials(initials);
		return user;
	}
    
    @Test
    public void testFindAll() {
    	List<User> users = new ArrayList<User>();
    	users.add(newUser("test1", "pw", "t1"));
    	users.add(newUser("test2", "pw", "t2"));
    	when(userRepository.findAll()).thenReturn(users);
    	
    	List<User> result = userService.findAll();
    	assertThat(result).isNotNull();
    	assertThat(result.size()).isEqualTo(users.size());
    }

    @Test
    public void testCreate() {
    	User user = newUser("test1", "pw", "t1");
    	when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encryptedPw");
    	when(userRepository.save(user)).thenReturn(user);
    	
    	User result = userService.create(user);
    	assertThat(result).isNotNull();
    	assertThat(result.getPassword()).isNotNull();
    	assertThat(result.getPassword()).isEqualTo("encryptedPw");
    }
    
    @Test
    public void testUpdate() {
    	User user = newUser("test1", "pw", "t1");
    	user.setId(1L);
    	when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    	when(userRepository.save(user)).thenReturn(user);
    	
    	User result = userService.update(user);
    	assertThat(result).isNotNull();
    	assertThat(result.getPassword()).isNotNull();
    	assertThat(result.getPassword()).isEqualTo("pw");
    	
    	// with password change
    	user.setPassword("pw2");
    	when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encryptedPw");
    	when(userRepository.findById(1L)).thenReturn(Optional.of(newUser("test1", "encryptedPw2", "t1")));
    	when(userRepository.save(user)).thenReturn(user);
    	result = userService.update(user);
    	assertThat(result).isNotNull();
    	assertThat(result.getPassword()).isNotNull();
    	assertThat(result.getPassword()).isEqualTo("encryptedPw");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUpdateWithoutExistingUser() {
    	User user = newUser("test1", "pw", "t1");
    	user.setId(1L);
    	when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(null));    	
    	userService.update(user);
    }
    
    @Test
    public void testDelete() {
    	User user = newUser("test1", "pw", "t1");
    	userService.delete(user);
    	
    	verify(userRepository, times(1)).delete(user);
    }
    
    @Test
    public void testFindByName() {
    	User user = newUser("test1", "pw", "t1");
    	when(userRepository.findByName("test1")).thenReturn(user);

    	User result = userService.findByName("test1");
    	assertThat(result).isNotNull();
    	assertThat(result.getName()).isEqualTo("test1");
    }

    @Test
    public void testFindById() {
    	User user = newUser("test1", "pw", "t1");
    	user.setId(1L);
    	when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    	when(userRepository.findById(-1L)).thenReturn(Optional.ofNullable(null));

    	User result = userService.findById(1L);
    	assertThat(result).isNotNull();
    	assertThat(result.getName()).isEqualTo("test1");
    	
    	result = userService.findById(-1L);
    	assertThat(result).isNull();
    	result = userService.findById(null);
    	assertThat(result).isNull();
    }
}
