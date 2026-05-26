package com.ian.web.systemsettings.learning_type;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningTypeRepository extends JpaRepository<LearningType, Long> {

}
