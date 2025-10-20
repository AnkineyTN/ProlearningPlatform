package com.cabybara.prolearningplatform.mapper.helpers;

import java.time.OffsetDateTime;

public class DateTimeMapper {

    // Converts OffsetDateTime to epoch milliseconds (long)
    public static Long offsetDateTimeToEpochMilli(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return 0L;
        }
        return dateTime.toInstant().toEpochMilli();
    }
}