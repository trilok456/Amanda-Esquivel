package com.maillSaviour.demo.Repository;


import com.maillSaviour.demo.Entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    Optional<MessageEntity> findByMessageName(String messageName);
    // Additional custom query methods can be added here if needed
}

