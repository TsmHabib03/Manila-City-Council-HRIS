package com.ian.web.sysuser;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource
public interface SysUserRepository  extends JpaRepository<SysUser, Long> {
	Optional<SysUser> findByUsername(String username);
}
