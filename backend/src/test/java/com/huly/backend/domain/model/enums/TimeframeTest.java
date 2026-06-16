package com.huly.backend.domain.model.enums;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.ZoneId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeframeTest {

    @Test
    void testFromString() {
        assertThat(Timeframe.fromString(null)).isEqualTo(Timeframe.TOTAL);
        assertThat(Timeframe.fromString("today")).isEqualTo(Timeframe.TODAY);
        assertThat(Timeframe.fromString("  WEEK  ")).isEqualTo(Timeframe.WEEK);
        assertThat(Timeframe.fromString("MONTH")).isEqualTo(Timeframe.MONTH);
        
        assertThatThrownBy(() -> Timeframe.fromString("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid timeframe: INVALID");
    }

    @Test
    void testGetStartInstant() {
        Instant now = Instant.now();
        ZoneId zone = ZoneId.systemDefault();
        
        assertThat(Timeframe.TODAY.getStartInstant(now, zone)).isNotNull();
        assertThat(Timeframe.WEEK.getStartInstant(now, zone)).isNotNull();
        assertThat(Timeframe.MONTH.getStartInstant(now, zone)).isNotNull();
        assertThat(Timeframe.TOTAL.getStartInstant(now, zone)).isNull();
    }
}
