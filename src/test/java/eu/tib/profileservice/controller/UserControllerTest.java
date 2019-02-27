package eu.tib.profileservice.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.service.UserService;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private UserService userService;
	
	private User newUser(final Long id, final String name, final String password, final String initials) {
		final User user = new User();
		user.setId(id);
		user.setName(name);
		user.setPassword(password);
		user.setInitials(initials);
		return user;
	}
	
	@Test
	@WithMockUser(authorities = {"MANAGE_USERS"})
	public void testShowUser() throws Exception {
    	User user = newUser(1L, "Username Test ABCD", "pw", "t1");
    	when(userService.findById(1L)).thenReturn(user);
    	
    	mvc.perform(get(UserController.BASE_PATH + UserController.PATH_SHOW + "/{id}/", user.getId()))
    		.andExpect(status().isOk())    		
    		.andExpect(content().string(containsString(user.getName())));
    	
    	when(userService.findById(2L)).thenReturn(null);
    	mvc.perform(get(UserController.BASE_PATH + UserController.PATH_SHOW + "/{id}/", 2L))
    		.andExpect(redirectedUrl(UserController.BASE_PATH + UserController.PATH_LIST))
    		.andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE, HomeController.INFO_MESSAGE_TYPE_ERROR))
    		.andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE, UserController.CODE_MESSAGE_USER_NOTFOUND));
	}
	
	// TODO

}
