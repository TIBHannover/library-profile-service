package eu.tib.profileservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.domain.User.Role;
import eu.tib.profileservice.repository.UserRepository;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UserDetailsServiceTest {

  @TestConfiguration
  static class TestContextConfiguration {
    @Bean
    public UserDetailsService service() {
      return new UserDetailsServiceImpl();
    }
  }

  @Autowired
  private UserDetailsService userDetailsService;
  @MockBean
  private UserRepository userRepository;

  private User newUser(final String name, final String password, final Role... roles) {
    final User user = new User();
    user.setName(name);
    user.setPassword(password);
    user.setRoles(Arrays.asList(roles));
    return user;
  }

  @Test
  public void testLoadUserByUsername() {
    User user = newUser("test", "pw", Role.PROCESS_DOCUMENTS);
    when(userRepository.findByName(Mockito.anyString())).thenReturn(user);

    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getName());
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(user.getName());
    assertThat(userDetails.getPassword()).isNotNull();
    assertThat(userDetails.getAuthorities()).isNotNull();
    assertThat(userDetails.getAuthorities().size()).isEqualTo(1);
  }

  @Test(expected = UsernameNotFoundException.class)
  public void testUsernameNorFound() {
    when(userRepository.findByName(Mockito.anyString())).thenReturn(null);
    userDetailsService.loadUserByUsername("test");
  }

}
