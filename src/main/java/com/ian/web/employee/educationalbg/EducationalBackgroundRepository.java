package com.ian.web.employee.educationalbg;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.web.employee.Employee;
import com.ian.web.systemsettings.degree_courses.DegreeCourses;


@Repository
public interface EducationalBackgroundRepository extends JpaRepository<EducationalBackground, Long> {
//    List<EducationalBackground> findAllByEmployee(Employee employee);
    
	Optional<EducationalBackground> findByEmployeeAndDegreeCourse(Employee employee, DegreeCourses degreeCourse);
    List<EducationalBackground> findByEmployeeId(long employeeId);
    boolean existsByAcademicHonorsId(Long academicHonorsId);
    boolean existsByDegreeCourseId(Long degreeCourseId);
    boolean existsByDegreeLevelId(Long degreeLevelId);
    boolean existsByScholarshipId(Long scholarshipId);
    boolean existsBySchoolId(Long schoolId);
//    EducationalBackground findByEmployeeIdAndRelationship(long employeeId, String relationship);
}
