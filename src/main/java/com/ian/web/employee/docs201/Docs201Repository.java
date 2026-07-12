package com.ian.web.employee.docs201;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Docs201Repository extends JpaRepository<Docs201, Long> {
	
	List<Docs201> findByEmployeeId(long employeeId);
	boolean existsByDocumentTypeId(Long documentTypeId);

}
