package com.ian.web.systemsettings.district;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictRepository   extends JpaRepository<District, Long> {
	Optional<District> findByDistrictName(String districtName);
}
