package com.example.demo.repository;

import com.example.demo.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link ChatMessage} persistence.
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** Returns all messages for a given connection, ordered chronologically. */
    List<ChatMessage> findByConnectionIdOrderBySentAtAsc(Long connectionId);

    /** Finds the latest message entry for a stored file URL. */
    Optional<ChatMessage> findTopByFileUrlOrderByIdDesc(String fileUrl);
}
