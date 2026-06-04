package com.cabybara.prolearningplatform.enums;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public enum TrendingPeriod {
    H24, D7, D30, D365, ALL_TIME;

    private static final ZoneId ICT = ZoneId.of("Asia/Ho_Chi_Minh");

    public OffsetDateTime toSinceDateTime() {
        OffsetDateTime now = OffsetDateTime.now(ICT);
        return switch (this) {
            case H24      -> now.minusHours(24);
            case D7       -> now.minusDays(7);
            case D30      -> now.minusDays(30);
            case D365     -> now.minusDays(365);
            case ALL_TIME -> OffsetDateTime.parse("1970-01-01T00:00:00Z");
        };
    }

    public LocalDate toSinceLocalDate() {
        LocalDate now = LocalDate.now(ICT);
        return switch (this) {
            case H24      -> now.minusDays(1);
            case D7       -> now.minusDays(7);
            case D30      -> now.minusDays(30);
            case D365     -> now.minusDays(365);
            case ALL_TIME -> LocalDate.of(1970, 1, 1);
        };
    }
}
