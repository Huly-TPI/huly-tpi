package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.journal.CreateJournalEntryMapper;
import com.huly.backend.domain.mapper.journal.ListJournalEntriesMapper;
import com.huly.backend.domain.repository.journal.JournalEntryRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.journal.CreateJournalEntryUseCase;
import com.huly.backend.domain.useCase.journal.ListJournalEntriesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JournalUseCaseConfig {

    @Bean
    public CreateJournalEntryMapper createJournalEntryMapper() {
        return new CreateJournalEntryMapper();
    }

    @Bean
    public ListJournalEntriesMapper listJournalEntriesMapper() {
        return new ListJournalEntriesMapper();
    }

    @Bean
    public CreateJournalEntryUseCase createJournalEntryUseCase(JournalEntryRepository journalEntryRepository,
                                                               UserVectorMemoryService userVectorMemoryService,
                                                               CreateJournalEntryMapper createJournalEntryMapper) {
        return new CreateJournalEntryUseCase(journalEntryRepository, userVectorMemoryService, createJournalEntryMapper);
    }

    @Bean
    public ListJournalEntriesUseCase listJournalEntriesUseCase(JournalEntryRepository journalEntryRepository,
                                                               ListJournalEntriesMapper listJournalEntriesMapper) {
        return new ListJournalEntriesUseCase(journalEntryRepository, listJournalEntriesMapper);
    }
}
