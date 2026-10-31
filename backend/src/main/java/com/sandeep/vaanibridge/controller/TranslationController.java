package com.sandeep.vaanibridge.controller;

import com.sandeep.vaanibridge.dto.TranslationRequest;
import com.sandeep.vaanibridge.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/translate")
@CrossOrigin(origins = "*")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @PostMapping
    public ResponseEntity<String> translate(@RequestBody TranslationRequest request) {
        try {
            String translatedText = translationService.translate(
                    request.getText(),
                    request.getSourceLang(),
                    request.getTargetLang()
            );
            return ResponseEntity.ok(translatedText);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Translation error: " + e.getMessage());
        }
    }
}
