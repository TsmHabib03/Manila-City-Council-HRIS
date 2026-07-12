package com.ian.web.employee;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface EmployeeRepository  extends JpaRepository<Employee, Long> {
	Optional<Employee> findByPlantillaNo(String plantillaNo);
	List<Employee> findByFirstNameAndLastName(String firstName, String lastName);
	Optional<Employee> findByEmpNo(String empNo);
	Optional<Employee> findByUsername(String username);
	Optional<Employee> findByIdAndEmpHashCode(long id, String empHashCode);
	Optional<Employee> findById(long id);
	List<Employee> findByEmpNoOrPlantillaNo(String empNo, String plantillaNo);
    long countByEmployeeStatusIsNull();
    long countByDivisionIsNull();
    boolean existsByDistrictId(Long districtId);
    boolean existsByDivisionId(Long divisionId);
    boolean existsByEmployeeStatusId(Long employeeStatusId);
    boolean existsByPositionTitleId(Long positionTitleId);

	List<Employee> findByFirstNameAndLastNameAndBirthdate(String firstName, String lastName, LocalDate birthDate);
	
//	@Query("SELECT e.employeeStatus, COUNT(e) FROM Employee e GROUP BY e.employeeStatus")
//	Map<Long, Long> countEmployeeStatus();
	
    @Query(value = "SELECT division_id, COUNT(*) FROM employee WHERE division_id IS NOT NULL GROUP BY division_id", nativeQuery = true)
	List<Object[]> countEmployeeDivision();
	
    @Query(value = "SELECT employee_status_id, COUNT(*) FROM employee WHERE employee_status_id IS NOT NULL GROUP BY employee_status_id", nativeQuery = true)
	List<Object[]> countEmployeeStatus();

    @Query(value = "SELECT UPPER(gender) AS gender, COUNT(*) FROM employee GROUP BY UPPER(gender)", nativeQuery = true)
    List<Object[]> countEmployeeGender();
	
	@Query("SELECT e FROM Employee e WHERE MONTH(e.birthdate) = MONTH(CURRENT_DATE)")
    List<Employee> findEmployeesWithBirthMonth();

    default Map<Long, Long> getCountEmployeeStatus() {
        List<Object[]> result = countEmployeeStatus();
        Map<Long, Long> statusCounts = new HashMap<>();
        for (Object[] row : result) {
            //statusCounts.put((Long) row[0], (Long) row[1]);
        	BigInteger statusIdBigInt = (BigInteger) row[0];
            BigInteger countBigInt = (BigInteger) row[1];

            // Convert BigInteger to Long
            Long statusId = statusIdBigInt.longValue();
            Long count = countBigInt.longValue();

            // Now, you can put the values into the map
            statusCounts.put(statusId, count);
        }
        return statusCounts;
    }
    
    default Map<Long, Long> getCountEmployeeDivision() {
        List<Object[]> result = countEmployeeDivision();
        Map<Long, Long> statusCounts = new HashMap<>();
        for (Object[] row : result) {
            //statusCounts.put((Long) row[0], (Long) row[1]);
        	BigInteger statusIdBigInt = (BigInteger) row[0];
            BigInteger countBigInt = (BigInteger) row[1];

            // Convert BigInteger to Long
            Long statusId = statusIdBigInt.longValue();
            Long count = countBigInt.longValue();

            // Now, you can put the values into the map
            statusCounts.put(statusId, count);
        }
        return statusCounts;
    }
}
