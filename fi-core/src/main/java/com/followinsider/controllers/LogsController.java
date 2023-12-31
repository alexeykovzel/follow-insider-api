package com.followinsider.controllers;

import com.followinsider.core.logging.Log;
import com.followinsider.core.logging.LogLevel;
import com.followinsider.core.logging.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogsController {

    private final LogService logService;

    @PostMapping("/ping")
    public void ping() {
        logService.ping();
    }

    @GetMapping
    public List<Log> getFileLogs(
            @RequestParam(value = "level", defaultValue = "INFO") LogLevel level,
            @RequestParam(value = "file", defaultValue = "app.log") String file,
            @RequestParam(value = "inverse", defaultValue = "true") boolean inverse
    ) throws IOException {
        return logService.getFileLogs(file, level, inverse);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleIOException(IOException e) {
        return e.getMessage();
    }

}
