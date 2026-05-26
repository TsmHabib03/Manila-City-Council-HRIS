package com.ian.web.systemsettings.position_title;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PositionTitleRepository extends JpaRepository<PositionTitle, Long> {
	Optional<PositionTitle> findByPositionTitleName(String positionTitleName);
	List<PositionTitle> findAllByOrderByPositionTitleNameAsc();
}
