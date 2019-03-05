package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Transactional(readOnly = true)
  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    final User user = userRepository.findByName(username);

    if (user == null) {
      throw new UsernameNotFoundException("user '" + username + "' not found");
    }

    final List<GrantedAuthority> authorities = new ArrayList<>();
    for (final User.Role role : user.getRoles()) {
      authorities.add(new SimpleGrantedAuthority(role.toString()));
    }

    return new org.springframework.security.core.userdetails.User(user.getName(), user
        .getPassword(), authorities);
  }

}
