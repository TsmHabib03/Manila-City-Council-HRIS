package com.ian.web.employee.archive;

import java.util.List;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import lombok.RequiredArgsConstructor;

/**
 * Makes the current Archive folder list available as ${archiveSections} on
 * every page, so the shared sidebar fragment (fragments/common.html) can
 * render admin-added folders without every controller passing it in.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ArchiveSectionsAdvice {

	private final ArchiveSectionRepository archiveSectionRepository;

	@ModelAttribute("archiveSections")
	public List<ArchiveSection> archiveSections() {
		return archiveSectionRepository.findAllByOrderBySortOrderAscDisplayNameAsc();
	}
}
