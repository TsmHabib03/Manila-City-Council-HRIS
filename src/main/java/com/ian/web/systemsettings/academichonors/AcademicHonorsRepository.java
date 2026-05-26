package com.ian.web.systemsettings.academichonors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicHonorsRepository   extends JpaRepository<AcademicHonors, Long> {

}
