package com.ian.web.employee.attachment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAttachmentRepository extends JpaRepository<DocumentAttachment, Long> {

    List<DocumentAttachment> findByModuleAndRecordId(String module, Long recordId);

    void deleteAllByModuleAndRecordId(String module, Long recordId);
}
