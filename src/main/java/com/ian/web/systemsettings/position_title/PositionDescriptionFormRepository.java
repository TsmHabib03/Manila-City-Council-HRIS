package com.ian.web.systemsettings.position_title;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PositionDescriptionForm entity.
 */
@Repository
public interface PositionDescriptionFormRepository extends JpaRepository<PositionDescriptionForm, Long> {

    // You can add custom query methods here if needed.
    // For example, to find a form by its related PositionTitle:
    // List<PositionDescriptionForm> findByPositionTitle(PositionTitle positionTitle);

}