package eu.tib.profileservice.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.Category.Type;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.service.UserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@WithMockUser(authorities = {"MANAGE_USERS"})
public class UserControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private UserService userService;

  private Category newCategory(final Category.Type type, final String title) {
    Category category = new Category();
    category.setType(type);
    category.setCategory(title);
    return category;
  }

  private User newUser(final Long id, final String name, final String password,
      final String initials) {
    final User user = new User();
    user.setId(id);
    user.setName(name);
    user.setPassword(password);
    user.setInitials(initials);
    List<Category> categories = new ArrayList<>();
    categories.add(newCategory(Type.DDC, "500"));
    user.setCategories(categories);
    return user;
  }

  @Test
  public void testIndex() throws Exception {
    mvc.perform(get(UserController.BASE_PATH))
        .andExpect(redirectedUrl(UserController.BASE_PATH + UserController.PATH_LIST));
  }

  @Test
  public void testList() throws Exception {
    User user = newUser(1L, "Username Test ABCD", "pw", "t1");
    when(userService.findAll()).thenReturn(Arrays.asList(new User[] {user}));
    mvc.perform(get(UserController.BASE_PATH + UserController.PATH_LIST))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(user.getName())));
  }

  @Test
  public void testAddUser() throws Exception {
    mvc.perform(get(UserController.BASE_PATH + UserController.PATH_ADD))
        .andExpect(status().isOk());
  }

  @Test
  public void testShowUser() throws Exception {
    User user = newUser(1L, "Username Test ABCD", "pw", "t1");
    when(userService.findById(1L)).thenReturn(user);

    mvc.perform(get(UserController.BASE_PATH + UserController.PATH_SHOW + "/{id}/", user.getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(user.getName())));

    when(userService.findById(2L)).thenReturn(null);
    mvc.perform(get(UserController.BASE_PATH + UserController.PATH_SHOW + "/{id}/", 2L))
        .andExpect(redirectedUrl(UserController.BASE_PATH + UserController.PATH_LIST))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE,
            UserController.CODE_MESSAGE_USER_NOTFOUND));
  }

  @Test
  public void testEditUser() throws Exception {
    User user = newUser(1L, "Username Test ABCD", "pw", "t1");
    when(userService.findById(1L)).thenReturn(user);

    mvc.perform(get(UserController.BASE_PATH + UserController.PATH_EDIT + "/{id}/", user.getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(user.getName())));

    when(userService.findById(2L)).thenReturn(null);
    mvc.perform(get(UserController.BASE_PATH + UserController.PATH_EDIT + "/{id}/", 2L))
        .andExpect(redirectedUrl(UserController.BASE_PATH + UserController.PATH_LIST))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE,
            UserController.CODE_MESSAGE_USER_NOTFOUND));
  }

  @Test
  public void testCreateUser() throws Exception {
    User user = newUser(1L, "Username Test ABCD", "pw", "t1");

    mvc.perform(post(UserController.BASE_PATH + UserController.PATH_SAVE, user)
        .param(UserController.METHOD_CREATE, UserController.METHOD_CREATE).with(csrf()))
        .andExpect(redirectedUrl(UserController.BASE_PATH + UserController.PATH_LIST));

    verify(userService, times(1)).create(Mockito.any(User.class));
  }

  @Test
  public void testUpdateUser() throws Exception {
    User user = newUser(1L, "Username Test ABCD", "pw", "t1");

    mvc.perform(post(UserController.BASE_PATH + UserController.PATH_SAVE, user)
        .param(UserController.METHOD_EDIT, UserController.METHOD_CREATE).with(csrf()))
        .andExpect(redirectedUrl(UserController.BASE_PATH + UserController.PATH_LIST));

    verify(userService, times(1)).update(Mockito.any(User.class));
  }

  @Test
  public void testDeleteUser() throws Exception {
    User user = newUser(1L, "Username Test ABCD", "pw", "t1");

    mvc.perform(post(UserController.BASE_PATH + UserController.PATH_DELETE, user).with(csrf()))
        .andExpect(redirectedUrl(UserController.BASE_PATH + UserController.PATH_LIST));

    verify(userService, times(1)).delete(Mockito.any(User.class));
  }

}
