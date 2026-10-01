package com.app.service;

import com.app.repository.ClickEventRepository;
import com.app.repository.ShortUrlRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ClickEventRepository repo;
    private final ShortUrlRepository shortUrlRepo;

    public AnalyticsService(
            ClickEventRepository repo,
            ShortUrlRepository shortUrlRepo
    ) {
        this.repo = repo;
        this.shortUrlRepo = shortUrlRepo;
    }

    public Map<String, Object> get(String code) {

        // Verify short URL exists
        shortUrlRepo.findByCode(code)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Short link not found"
                        )
                );

        Pageable top5 = PageRequest.of(0, 5);

        LocalDate today = LocalDate.now();

        LocalDateTime since = today.minusDays(6).atStartOfDay();

        Map<String, Object> out = new LinkedHashMap<>();

        out.put("code", code);

        out.put("totalClicks", repo.countByCode(code));
        out.put("uniqueVisitors", repo.countUniqueVisitors(code));
        out.put("topCountries", toMap(repo.topCountries(code, top5)));
        out.put("topReferrers", toMap(repo.topReferrers(code, top5)));

        // Initialize all 7 days with zero
        Map<String, Long> perDay =
                new LinkedHashMap<>();

        for (int i = 6; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            perDay.put(date, 0L);
        }

        // Replace zero with actual click count
        for (Object[] row : repo.clicksPerDay(code, since)) {

            String date = row[0].toString();

            long count = ((Number) row[1]).longValue();

            perDay.put(date, count);
        }

        out.put("clicksLast7Days", perDay);

        return out;
    }

    private List<Map<String, Object>> toMap(List<Object[]> rows) {
        return rows.stream()
                .map(r -> Map.of("name", (Object) String.valueOf(r[0]), "count", r[1]))
                .toList();
    }
}