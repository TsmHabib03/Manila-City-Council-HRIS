package com.ian.web.systemsettings.civil_status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CivilStatusRepository extends JpaRepository<CivilStatus, Long> {

}
