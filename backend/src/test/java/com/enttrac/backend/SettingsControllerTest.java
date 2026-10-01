package com.enttrac.backend;

import com.enttrac.backend.auth.JwtService;
import com.enttrac.backend.controller.SettingsController;
import com.enttrac.backend.model.item.SettingsItem;
import com.enttrac.backend.repository.SettingsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
@Import(JwtService.class)
public class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SettingsRepository settingsRepository;

    private static final String USER_ID = "USER#google#google-sub-123";

    // --- GET /api/settings ---

    @Test
    void getSettings_ShouldReturnTabs_WhenSettingsExist() throws Exception {
        String accessToken = jwtService.generateAccessToken(USER_ID);

        SettingsItem item = new SettingsItem();
        item.setTabsJson("[{\"id\":\"book\",\"enabled\":true}]");

        when(settingsRepository.find(USER_ID)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/settings")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("book"))
                .andExpect(jsonPath("$[0].enabled").value(true));
    }

    @Test
    void getSettings_ShouldReturn404_WhenNoSettingsFound() throws Exception {
        String accessToken = jwtService.generateAccessToken(USER_ID);

        when(settingsRepository.find(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/settings")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSettings_ShouldReturn500_WhenTabsJsonIsInvalid() throws Exception {
        String accessToken = jwtService.generateAccessToken(USER_ID);

        SettingsItem item = new SettingsItem();
        item.setTabsJson("not valid json {{");

        when(settingsRepository.find(USER_ID)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/settings")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/settings ---

    @Test
    void saveSettings_ShouldCreateNewItem_WhenNoneExists() throws Exception {
        String accessToken = jwtService.generateAccessToken(USER_ID);

        when(settingsRepository.find(USER_ID)).thenReturn(Optional.empty());

        List<Map<String, Object>> tabs = List.of(Map.of("id", "book", "enabled", true));

        mockMvc.perform(put("/api/settings")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tabs)))
                .andExpect(status().isOk());

        verify(settingsRepository).save(argThat(saved ->
                saved.getPk().equals(USER_ID) &&
                        saved.getSk().equals("SETTINGS") &&
                        saved.getTabsJson().contains("book")
        ));
    }

    @Test
    void saveSettings_ShouldUpdateExistingItem_WhenOneExists() throws Exception {
        String accessToken = jwtService.generateAccessToken(USER_ID);

        SettingsItem existing = new SettingsItem();
        existing.setPk(USER_ID);
        existing.setSk("SETTINGS");
        existing.setTabsJson("[{\"id\":\"book\",\"enabled\":false}]");

        when(settingsRepository.find(USER_ID)).thenReturn(Optional.of(existing));

        List<Map<String, Object>> tabs = List.of(Map.of("id", "book", "enabled", true));

        mockMvc.perform(put("/api/settings")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tabs)))
                .andExpect(status().isOk());

        verify(settingsRepository).save(argThat(saved ->
                saved.getTabsJson().contains("true")
        ));
    }
}
