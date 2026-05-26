package com.ian.web.systemsettings.schools;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
	Optional<School> findBySchoolName(String schoolName);
	List<School> findAllByOrderBySchoolNameAsc();
}
