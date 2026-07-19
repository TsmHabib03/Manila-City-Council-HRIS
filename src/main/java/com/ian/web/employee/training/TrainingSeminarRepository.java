package com.ian.web.employee.training;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingSeminarRepository extends JpaRepository<TrainingSeminar, Long> {

	List<TrainingSeminar> findAllByOrderByTrainingDateDesc();

}
