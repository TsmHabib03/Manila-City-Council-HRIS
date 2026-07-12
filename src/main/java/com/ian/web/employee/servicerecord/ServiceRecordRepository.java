package com.ian.web.employee.servicerecord;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRecordRepository  extends JpaRepository<ServiceRecord, Long> {
	
	List<ServiceRecord> findByEmployeeId(long employeeId);
	List<ServiceRecord> findByEmployeeIdOrderByDateFromDesc(long employeeId);
	ServiceRecord findTopByEmployeeIdOrderByEntranceDateDesc(long employeeId);
	ServiceRecord findTopByEmployeeIdOrderByDateFromDesc(long employeeId);
	boolean existsByEmployeeStatusId(Long employeeStatusId);
	boolean existsByPositionTitleId(Long positionTitleId);
	
	/**
     * Finds service records where the separation date matches the given numeric month and year.
     * @param month The month number (1-12)
     * @param year The year
     * @return A list of matching service records.
     */
    @Query("SELECT sr FROM ServiceRecord sr WHERE FUNCTION('MONTH', sr.separationDate) = :month AND FUNCTION('YEAR', sr.separationDate) = :year")
    List<ServiceRecord> findBySeparationDateMonthAndYear(@Param("month") int month, @Param("year") int year);
    
    /**
     * Finds service records where the separation date matches the given numeric month and year.
     * @param month The month number (1-12)
     * @param year The year
     * @return A list of matching service records.
     */
    @Query("SELECT sr FROM ServiceRecord sr WHERE FUNCTION('MONTH', sr.entranceDate) = :month AND FUNCTION('YEAR', sr.entranceDate) = :year")
    List<ServiceRecord> findByEntranceDateMonthAndYear(@Param("month") int month, @Param("year") int year);

}
