package com.app.service;

import com.app.util.Base62;
import com.app.entity.ShortUrl;
import com.app.repository.ShortUrlRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class UrlService {

        private final ShortUrlRepository repo;

        //caffeine cache
        private final Cache<String, ShortUrl> cache = Caffeine.newBuilder()
                .maximumSize(10_000)  // 10,000 shortUrl objects in memory
                .expireAfterWrite(Duration.ofMinutes(10))   // expire after 10 min
                .build();

        public UrlService(ShortUrlRepository repo) { this.repo = repo; }
/*
            longUrl    → original URL
            alias      → optional custom short code
            expiresAt  → optional expiry
            for example:-
            longUrl:https://github.com/kalpana23019
            alias:github
           expiresAt:null
 */
        public ShortUrl create(String longUrl, String alias, LocalDateTime expiresAt) {


            // Url validation :- start with http and https



            if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://"))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL must start with http:// or https://");

            // create an entity where all fields are null
            ShortUrl s = new ShortUrl();
            s.setLongUrl(longUrl);
            s.setExpiresAt(expiresAt);


            //custom alias :checks whether user provided an alias
            //exmaple :https://localhost:8080/github

            if (alias != null && !alias.isBlank()) {

                //should be matches the alias
                if (!alias.matches("[A-Za-z0-9_-]{3,32}"))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid alias");

                //if alias already exists
                if (repo.existsByCode(alias))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Alias already taken");
                s.setCode(alias);
                return repo.save(s);
            }

            s.setCode("tmp-" + UUID.randomUUID());   // placeholder, first id doesn't exit so  needs id first , save id
            s = repo.save(s);
            s.setCode(Base62.encode(s.getId() + 100_000)); // offset → min 3 chars
            return repo.save(s);
        }

        /* cache : - caffeine
 if request is already present then it return the response
 Request -> Caffeine->ShortUrl
     or it will hit database and return
Request->Caffeine->MISS->MySQL->ShortUrl*/

    public ShortUrl resolve(String code) {

        ShortUrl s = cache.get(code, c -> repo.findByCode(c).orElse(null));

            if (s == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short link not found");
            if (!s.isActive()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This link has been deactivated");
            if (s.getExpiresAt() != null && s.getExpiresAt().isBefore(LocalDateTime.now()))
                throw new ResponseStatusException(HttpStatus.GONE, "This link has expired");
            return s;
        }

        /*
        deactivate :- find URl -> active = false -> save ->remove from cache
         */
        public void deactivate(String code) {
            ShortUrl s = repo.findByCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            s.setActive(false);
            repo.save(s);
            cache.invalidate(code);  //remove stale from cache value
        }

        public boolean exists(String code) { return repo.existsByCode(code); }
    }
