package org.example.proiect.repository;

import org.example.proiect.model.Price;
import org.example.proiect.model.Symbol;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findBySymbolOrderByTimestampDesc(Symbol symbol, Pageable pageable);

    Optional<Price> findTopBySymbolOrderByTimestampDesc(Symbol symbol);

    @Query("SELECT p FROM Price p WHERE p.symbol = :symbol AND p.timestamp >= :since ORDER BY p.timestamp DESC")
    List<Price> findRecentPrices(@Param("symbol") Symbol symbol, @Param("since") LocalDateTime since);

    @Query("SELECT p FROM Price p WHERE p.symbol.ticker = :ticker ORDER BY p.timestamp DESC")
    List<Price> findByTickerOrderByTimestampDesc(@Param("ticker") String ticker, Pageable pageable);

    @Query("SELECT DISTINCT p.symbol FROM Price p")
    List<Symbol> findDistinctSymbols();

    @Query(value = "SELECT p.* FROM prices p " +
            "INNER JOIN (SELECT symbol_id, MAX(timestamp) as max_ts FROM prices GROUP BY symbol_id) latest " +
            "ON p.symbol_id = latest.symbol_id AND p.timestamp = latest.max_ts", nativeQuery = true)
    List<Price> findLatestPricesForAllSymbols();
}

