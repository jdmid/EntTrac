package com.enttrac.backend.controller;

import com.enttrac.backend.auth.CurrentUserId;
import com.enttrac.backend.model.item.SettingsItem;
import com.enttrac.backend.repository.SettingsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SettingsController {

    private final SettingsRepository settingsRepository;
    private final ObjectMapper objectMapper;

    public SettingsController(SettingsRepository settingsRepository, ObjectMapper objectMapper) {
        this.settingsRepository = settingsRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getSettings(@CurrentUserId String userId) {
        Optional<SettingsItem> item = settingsRepository.find(userId);
        if (item.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            List<Map<String, Object>> tabs = objectMapper.readValue(
                    item.get().getTabsJson(),
                    new TypeReference<List<Map<String, Object>>>() {});
            return ResponseEntity.ok(tabs);
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping
    public ResponseEntity<Void> saveSettings(@CurrentUserId String userId,
                                             @RequestBody List<Map<String, Object>> tabs) throws JsonProcessingException {
        SettingsItem item = settingsRepository.find(userId).orElseGet(SettingsItem::new);
        item.setPk(userId);
        item.setSk("SETTINGS");
        item.setTabsJson(objectMapper.writeValueAsString(tabs));
        item.setUpdatedAt(Instant.now().toString());
        settingsRepository.save(item);
        return ResponseEntity.ok().build();
    }
}
