package eu.tib.profileservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import eu.tib.profileservice.controller.DocumentController;
import eu.tib.profileservice.controller.UserController;
import eu.tib.profileservice.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
//    		.authorizeRequests().antMatchers("/").permitAll()
				//.authorizeRequests().antMatchers("/").permitAll().anyRequest().authenticated()
				.authorizeRequests().antMatchers(UserController.BASE_PATH + "/**").hasAuthority("MANAGE_USERS")
				.and().authorizeRequests().antMatchers(DocumentController.BASE_PATH + "/**").hasAuthority("PROCESS_DOCUMENTS")
				.and().formLogin().permitAll()
				.and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
	}

//    public AuthenticationFailureHandler authenticationFailureHandler() {
//      return new SimpleUrlAuthenticationFailureHandler("/403");
//    }

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}

}
