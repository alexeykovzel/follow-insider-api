package com.followinsider.core.logging;

import java.time.LocalDate;

public record Log(

        LocalDate date,

        String thread,

        LogLevel level,

        String path,

        String message,

        String stackTrace

) {}
