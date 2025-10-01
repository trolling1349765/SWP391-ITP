package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
