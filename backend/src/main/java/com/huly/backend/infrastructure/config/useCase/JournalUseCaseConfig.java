package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.JournalEntryRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.journal.CreateJournalEntryUseCase;
import com.huly.backend.domain.useCase.journal.ListJournalEntriesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JournalUseCaseConfig {

    @Bean
    public CreateJournalEntryUseCase createJournalEntryUseCase(JournalEntryRepository journalEntryRepository, UserVectorMemoryService userVectorMemoryService) {
        return new CreateJournalEntryUseCase(journalEntryRepository, userVectorMemoryService);
    }

    @Bean
    public ListJournalEntriesUseCase listJournalEntriesUseCase(JournalEntryRepository journalEntryRepository) {
        return new ListJournalEntriesUseCase(journalEntryRepository);
    }
}
