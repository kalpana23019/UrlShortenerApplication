package com.app.service;

import com.app.entity.ClickEvent;
import com.app.repository.ClickEventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;
import ua_parser.Parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class ClickService {

    private final ClickEventRepository repo;
    private final RestTemplate rest = new RestTemplate();
    private final Parser uaParser;

    public ClickService(ClickEventRepository repo) throws IOException {
        this.repo = repo;
        this.uaParser = new Parser();
    }

    @Async
    public void record(
            String code,
            String ip,
            String userAgent,
            String referrer
    ) {

        try {

            // -------------------------
            // 1. Browser
            // -------------------------

            String browser = "Unknown";

            if (userAgent != null && !userAgent.isBlank()) {
                browser = uaParser
                        .parseUserAgent(userAgent)
                        .family;
            }


            // -------------------------
            // 2. Device
            // -------------------------

            String device = "Desktop";

            if (userAgent != null) {

                if (userAgent.contains("Tablet")
                        || userAgent.contains("iPad")) {

                    device = "Tablet";

                } else if (userAgent.contains("Mobi")) {

                    device = "Mobile";
                }
            }


            // -------------------------
            // 3. Referrer
            // -------------------------

            String ref =
                    (referrer == null || referrer.isBlank())
                            ? "Direct"
                            : referrer;


            // -------------------------
            // 4. Visitor hash
            // -------------------------

            String visitorData =
                    String.valueOf(ip)
                            + "|"
                            + String.valueOf(userAgent);

            String hash =
                    DigestUtils.md5DigestAsHex(
                            visitorData.getBytes(StandardCharsets.UTF_8)
                    );


            // -------------------------
            // 5. Country
            // -------------------------

            String country = lookupCountry(ip);


            // -------------------------
            // 6. Save click event
            // -------------------------

            ClickEvent event = new ClickEvent(
                    code,
                    country,
                    browser,
                    device,
                    ref,
                    hash
            );

            repo.save(event);

        } catch (Exception e) {

            // Analytics failure must never break redirect
            System.err.println(
                    "Failed to record click analytics: "
                            + e.getMessage()
            );
        }
    }


    private String lookupCountry(String ip) {

        if (ip == null || ip.isBlank()) {
            return "Unknown";
        }
        //Localhost testing
        if(ip.equals("127.0.0.1")
        || ip.equals("::1")
        || ip.equals("0:0:0:0:0:0:0:1")){

            return "India";
        }

        try {

            Map<?, ?> res =
                    rest.getForObject(
                            "http://ip-api.com/json/"
                                    + ip
                                    + "?fields=country",
                            Map.class
                    );

            Object country =
                    res == null
                            ? null
                            : res.get("country");

            return country == null
                    ? "Unknown"
                    : country.toString();

        } catch (Exception e) {

            System.out.println("Country lookup failed for IP:" + ip);
            return "Unknown";
        }
    }
}