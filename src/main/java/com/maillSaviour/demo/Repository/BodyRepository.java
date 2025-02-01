package com.maillSaviour.demo.Repository;

import com.maillSaviour.demo.Entity.BodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BodyRepository extends JpaRepository<BodyEntity, Long> {
    Optional<BodyEntity> findByBody(String body);
}
