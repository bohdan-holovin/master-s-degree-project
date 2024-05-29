package com.holovin.diploma.controller;

import com.holovin.diploma.service.LocalBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backup")
public class LocalBackupController {
    private static final Logger logger = LoggerFactory.getLogger(LocalBackupController.class);

    private final LocalBackupService localBackupService;

    @Autowired
    public LocalBackupController(LocalBackupService localBackupService) {
        this.localBackupService = localBackupService;
    }

    @GetMapping("/create")
    public String createBackup(@RequestParam String sourceDirectory, @RequestParam String userId) {
        localBackupService.createBackup(sourceDirectory, userId);
        logger.info("Local backup process initiated for user {} in source directory: {}", userId, sourceDirectory);
        return "Local backup process initiated.";
    }

    @GetMapping("/restore")
    public String restoreBackup(@RequestParam String backupDirectory, @RequestParam String restoreDirectory, @RequestParam String userId) {
        localBackupService.restoreBackup(backupDirectory, restoreDirectory, userId);
        logger.info("Local restore process initiated for user {} from backup directory: {} to restore directory: {}", userId, backupDirectory, restoreDirectory);
        return "Local backup restored.";
    }

    @DeleteMapping("/delete")
    public String deleteBackup(@RequestParam String backupDirectory, @RequestParam String userId) {
        localBackupService.deleteBackup(backupDirectory, userId);
        logger.info("Local backup deleted for directory user: {}, {}", backupDirectory, userId);
        return "Local backup deleted.";
    }

    @PostMapping("/schedule")
    public String scheduleBackup(@RequestParam String sourceDirectory, @RequestParam String userId, @RequestParam int interval) {
        localBackupService.scheduleRegularBackup(sourceDirectory, userId, interval);
        logger.info("Local regular backup scheduled for user {} in source directory: {} with interval: {} hours", userId, sourceDirectory, interval);
        return "Local regular backup scheduled.";
    }
}
