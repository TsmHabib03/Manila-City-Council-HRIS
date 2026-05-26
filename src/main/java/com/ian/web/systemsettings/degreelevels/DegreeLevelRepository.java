package com.ian.web.systemsettings.degreelevels;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeLevelRepository extends JpaRepository<DegreeLevel, Long>{
	Optional<DegreeLevel> findByDegreeName(String degreeName);
}
