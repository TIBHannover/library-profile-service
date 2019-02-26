package eu.tib.profileservice.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.Category.Type;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.domain.User;

public class DocumentAssignmentFinderTest {
	
	private Category newDDCCategory(final String title) {
		Category category = new Category();
		category.setType(Type.DDC);
		category.setCategory(title);
		return category;
	}
	private User newUser(final String name, final String password, final String initials, final String... categories) {
		final User user = new User();
		user.setName(name);
		user.setPassword(password);
		user.setInitials(initials);
		user.setCategories(Arrays.asList(categories).stream().map(c->newDDCCategory(c)).collect(Collectors.toList()));
		return user;
	}
	private List<User> dummyUserList() {
		List<User> users = new ArrayList<User>();
		users.add(newUser("test1", "pw", "t1", "150", "160"));
		users.add(newUser("test2", "pw", "t2", "150.6"));
		users.add(newUser("test3", "pw", "t3", "100", "200"));
		users.add(newUser("test4", "pw", "t4"));
		User u5 = newUser("test5", "pw", "t5");
		u5.setCategories(null);
		users.add(u5);
		User u6 = newUser("test6", "pw", "t6", "invalid");
		u6.getCategories().get(0).setType(null);
		users.add(u6);
		return users;
	}
	private DocumentMetadata newDummyDocument(String... deweyDecimalClassifications) {
		DocumentMetadata documentMetadata = new DocumentMetadata();
		documentMetadata.setDeweyDecimalClassifications(new HashSet<String>(Arrays.asList(deweyDecimalClassifications)));
		return documentMetadata;
	}

	
	@Test
	public void testDetermineAssignee() {
		final DocumentAssignmentFinder helper = new DocumentAssignmentFinder(dummyUserList());

		User assignee = helper.determineAssignee(newDummyDocument("150.678"));
		assertThat(assignee).isNotNull();
		assertThat(assignee.getName()).isEqualTo("test2");

		assignee = helper.determineAssignee(newDummyDocument("160"));
		assertThat(assignee).isNotNull();
		assertThat(assignee.getName()).isEqualTo("test1");
		
		assignee = helper.determineAssignee(newDummyDocument("200", "150.6", "230.7"));
		assertThat(assignee).isNotNull();
		assertThat(assignee.getName()).isEqualTo("test2");
		
		assignee = helper.determineAssignee(newDummyDocument("204", "153.6"));
		assertThat(assignee).isNotNull();
		assertThat(assignee.getName()).isIn("test1", "test3"); // both with accuracy 2
		
		assignee = helper.determineAssignee(newDummyDocument("999"));
		assertThat(assignee).isNull();
		
		assignee = helper.determineAssignee(newDummyDocument());
		assertThat(assignee).isNull();
		
		assignee = helper.determineAssignee(new DocumentMetadata());
		assertThat(assignee).isNull();
	}

}
