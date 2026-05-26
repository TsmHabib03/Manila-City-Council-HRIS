package com.ian.web.systemsettings.degree_courses;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeCoursesRepository extends JpaRepository<DegreeCourses, Long> {
	Optional<DegreeCourses> findByDegreeCourseName(String degreeCourseName);
	List<DegreeCourses> findAllByOrderByDegreeCourseNameAsc();
}
