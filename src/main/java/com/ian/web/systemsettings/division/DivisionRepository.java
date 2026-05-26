package com.ian.web.systemsettings.division;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DivisionRepository  extends JpaRepository<Division, Long> {
	Optional<Division> findByDivisionName(String divisionName);
}
