insert into role (id, role) values (-1, 'MANAGE_USERS')
insert into role (id, role) values (-2, 'PROCESS_DOCUMENTS')

insert into user (email, initials, name, password, id) values ('test@abc.de', 'TM', 'test', '$2a$10$MfjKeK.5UjXXJBV0mZONKumJqJYVd//im/3F0oy.38b.aOmLuqhFW', -1) // pw xxx

insert into user_role (user_id, role_id) values (-1, -2)
insert into user_role (user_id, role_id) values (-1, -1)

insert into category (institution, category, id) values ('ABC', '200', -1)
insert into category (institution, category, id) values ('DEF', '30B', -2)
