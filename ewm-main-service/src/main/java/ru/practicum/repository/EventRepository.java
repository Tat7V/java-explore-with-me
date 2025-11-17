package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("SELECT e FROM Event e WHERE e.state IN :states")
    Page<Event> findAllByStateIn(List<EventState> states, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.state = 'PUBLISHED'")
    Page<Event> findAllPublishedEvents(Pageable pageable);

    List<Event> findByIdIn(List<Long> eventIds);

    @Query("""
            SELECT DISTINCT e
            FROM Event e
            LEFT JOIN FETCH e.initiator
            LEFT JOIN FETCH e.category
            WHERE e.state IN :states
              AND (:usersAll = true OR e.initiator.id IN :users)
              AND (:categoriesAll = true OR e.category.id IN :categories)
              AND (e.eventDate >= COALESCE(:rangeStart, e.eventDate))
              AND (e.eventDate <= COALESCE(:rangeEnd, e.eventDate))
            """)
    List<Event> findAllForAdminFiltered(
            @Param("users") List<Long> users,
            @Param("usersAll") boolean usersAll,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("categoriesAll") boolean categoriesAll,
            @Param("rangeStart") java.time.LocalDateTime rangeStart,
            @Param("rangeEnd") java.time.LocalDateTime rangeEnd
    );
}