package org.example.proiect.repository;

import org.example.proiect.model.Alert;
import org.example.proiect.model.Symbol;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findBySymbolOrderByTimestampDesc(Symbol symbol, Pageable pageable);

    List<Alert> findByIsReadFalseOrderByTimestampDesc();

    @Query("SELECT a FROM Alert a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<Alert> findRecentAlerts(@Param("since") LocalDateTime since);

    List<Alert> findByAlertTypeOrderByTimestampDesc(Alert.AlertType alertType, Pageable pageable);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.id = :id")
    void markAsRead(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.isRead = false")
    void markAllAsRead();

    long countByIsReadFalse();
}

