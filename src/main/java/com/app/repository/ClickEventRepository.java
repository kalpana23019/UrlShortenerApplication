package com.app.repository;

import com.app.entity.ClickEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByCode(String code);

    @Query("select count(distinct c.visitorHash) from ClickEvent c where c.code = :code")
    long countUniqueVisitors(String code);

    @Query("select c.country, count(c) from ClickEvent c where c.code = :code group by c.country order by count(c) desc")
    List<Object[]> topCountries(String code, Pageable pageable);

    @Query("select c.referrer, count(c) from ClickEvent c where c.code = :code group by c.referrer order by count(c) desc")
    List<Object[]> topReferrers(String code, Pageable pageable);

    @Query(value = "select date(clicked_at) d, count(*) from click_events where code = :code and clicked_at >= :since group by date(clicked_at) order by d", nativeQuery = true)
    List<Object[]> clicksPerDay(String code, LocalDateTime since);
}
