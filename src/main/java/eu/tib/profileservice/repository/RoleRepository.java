package eu.tib.profileservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.tib.profileservice.domain.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
