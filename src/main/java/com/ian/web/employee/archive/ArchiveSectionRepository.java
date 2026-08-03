package com.ian.web.employee.archive;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveSectionRepository extends JpaRepository<ArchiveSection, Long> {

	List<ArchiveSection> findAllByOrderBySortOrderAscDisplayNameAsc();

	boolean existsByCode(String code);

	Optional<ArchiveSection> findByCode(String code);

}
