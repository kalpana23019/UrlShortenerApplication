package com.app.controller;

import com.app.service.UrlService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;

@RestController
public class QrController {

    private final UrlService urls;

    public QrController(UrlService urls) {
        this.urls = urls;
    }

    @GetMapping(
            value = "/api/urls/{code}/qr",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public byte[] qr(
            @PathVariable String code,
            @RequestParam(defaultValue = "300") int size,
            HttpServletRequest http) throws Exception {

        // Check whether short URL exists
        if (!urls.exists(code)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Short link not found"
            );
        }

        // Keep QR size between 100 and 1000 pixels
        size = Math.max(100, Math.min(size, 1000));

        // Build short URL
        String base =
                http.getScheme()
                        + "://"
                        + http.getServerName()
                        + ":"
                        + http.getServerPort();

        String shortUrl = base + "/" + code;

        // Generate QR code
        BitMatrix matrix = new QRCodeWriter().encode(
                shortUrl,
                BarcodeFormat.QR_CODE,
                size,
                size
        );

        // Convert QR matrix to PNG
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(
                matrix,
                "PNG",
                out
        );

        return out.toByteArray();
    }
}