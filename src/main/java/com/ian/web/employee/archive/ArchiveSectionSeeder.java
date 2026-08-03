package com.ian.web.employee.archive;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * One-time seeding of the archive_section table (CR Request ID 023) so the
 * 3 folders that used to be hardcoded (SALN, Resigned Employees, Leaves)
 * keep working unchanged once sections become admin-manageable. Runs on
 * every boot but only inserts when the table is empty, so admin-added
 * folders and renames are never overwritten.
 */
@Component
@RequiredArgsConstructor
public class ArchiveSectionSeeder implements CommandLineRunner {

	private final ArchiveSectionRepository archiveSectionRepository;

	@Override
	public void run(String... args) {
		if (archiveSectionRepository.count() > 0) {
			return;
		}
		archiveSectionRepository.save(new ArchiveSection(0, "SALN", "SALN", 0));
		archiveSectionRepository.save(new ArchiveSection(0, "RESIGNED", "Resigned Employees", 1));
		archiveSectionRepository.save(new ArchiveSection(0, "LEAVES", "Leaves", 2));
	}
}
