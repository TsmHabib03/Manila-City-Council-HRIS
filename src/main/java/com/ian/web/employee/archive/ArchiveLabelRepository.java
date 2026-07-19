package com.ian.web.employee.archive;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveLabelRepository extends JpaRepository<ArchiveLabel, Long> {

	List<ArchiveLabel> findBySectionOrderByLabelNameAsc(String section);

}
