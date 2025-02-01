package com.maillSaviour.demo.Repository;

import com.maillSaviour.demo.Entity.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<EmailEntity, Long> {
    List<EmailEntity> findAllByBody_Id(Long bodyId);
}
