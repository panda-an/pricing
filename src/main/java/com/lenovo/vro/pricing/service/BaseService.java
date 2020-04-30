package com.lenovo.vro.pricing.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class BaseService {

    protected Date getInsertDate() {
        ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
        return Timestamp.valueOf(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(zonedDateTime));
    }

    protected String getStringDate(String format) {
        ZonedDateTime localDate = LocalDateTime.now().atZone(ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern(format, Locale.getDefault()).format(localDate);
    }

    protected String getFileDate() {
        ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()).format(zonedDateTime);
    }
}
