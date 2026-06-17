package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserSettingEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserSettingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExtensionSettingsRepositoryImpl implements ExtensionSettingsRepository {

    private final IUserSettingJpaRepository userSettingJpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Optional<ExtensionSettings> findByUserId(Long userId) {
        return userSettingJpaRepository.findByAppUser_Id(userId)
                .map(entity -> {
                    List<String> domains;
                    if (entity.getMonitoredDomains() == null || entity.getMonitoredDomains().trim().isEmpty()) {
                        domains = List.of("twitter.com", "x.com", "instagram.com", "tiktok.com", "youtube.com", "facebook.com");
                    } else {
                        domains = Arrays.stream(entity.getMonitoredDomains().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                    }
                    Integer pauseIntervalSeconds = entity.getPauseIntervalSeconds();
                    if (pauseIntervalSeconds == null) {
                        Integer pauseIntervalMinutes = entity.getPauseIntervalMinutes();
                        pauseIntervalSeconds = (pauseIntervalMinutes != null ? pauseIntervalMinutes : 20) * 60;
                    }
                    return ExtensionSettings.builder()
                            .enabled(entity.getAntiScrollEnabled() != null ? entity.getAntiScrollEnabled() : true)
                            .pauseIntervalSeconds(pauseIntervalSeconds)
                            .monitoredDomains(domains)
                            .dataSharingConsent(entity.getDataSharingConsent() != null ? entity.getDataSharingConsent() : false)
                            .build();
                });
    }

    @Override
    public void save(Long userId, ExtensionSettings settings) {
        UserSettingEntity entity = userSettingJpaRepository.findByAppUser_Id(userId)
                .orElseGet(() -> {
                    AppUserEntity user = appUserRepository.getReferenceById(userId);
                    return UserSettingEntity.builder().appUser(user).build();
                });

        entity.setAntiScrollEnabled(settings.isEnabled());
        entity.setPauseIntervalSeconds(settings.getPauseIntervalSeconds());
        entity.setPauseIntervalMinutes(settings.getPauseIntervalSeconds() / 60);
        entity.setDataSharingConsent(settings.isDataSharingConsent());
        
        if (settings.getMonitoredDomains() != null) {
            String domainsStr = String.join(",", settings.getMonitoredDomains());
            entity.setMonitoredDomains(domainsStr);
        } else {
            entity.setMonitoredDomains(null);
        }

        userSettingJpaRepository.save(entity);
    }
}
