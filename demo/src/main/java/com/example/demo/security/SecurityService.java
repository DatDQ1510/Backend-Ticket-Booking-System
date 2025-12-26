package com.example.demo.security;

import com.example.demo.context.UserContext;
import com.example.demo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final EventRepository eventRepository;

    // Kiểm tra xem user hiện tại có phải chủ event không
    public boolean isEventOwner(Long eventId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) return false;
        return eventRepository.findById(eventId)
                .map(event -> event.getUser().getUserId().equals(currentUserId))
                .orElse(false);
    }
}
