package com.ian.web.systemsettings.eligibility;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EligibilityRepository extends JpaRepository<Eligibility, Long> {
    
}
