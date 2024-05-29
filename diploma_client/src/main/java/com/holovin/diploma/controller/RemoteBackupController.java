package com.holovin.diploma.controller;

import com.holovin.diploma.service.RemoteBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/remote_backup")
public class RemoteBackupController {

    private final RemoteBackupService remoteBackupService;

    private static final Logger logger = LoggerFactory.getLogger(RemoteBackupController.class);

    @Autowired
    public RemoteBackupController( RemoteBackupService remoteBackupService) {
        this.remoteBackupService = remoteBackupService;
    }

    @GetMapping("/create")
    public String createBackup(@RequestParam String sourceDirectory, @RequestParam String userId, @RequestParam boolean isAws) {
        remoteBackupService.createBackup(sourceDirectory, userId, isAws);
        logger.info("Remote backup process initiated for user {} in source directory: {}", userId, sourceDirectory);
        return "Remote backup process initiated.";
    }

    @GetMapping("/restore")
    public String restoreBackup(@RequestParam String backupDirectory, @RequestParam String restoreDirectory, @RequestParam String userId, @RequestParam boolean isAws) throws IOException {
        remoteBackupService.restoreBackup(backupDirectory, restoreDirectory, userId, isAws);
        logger.info("Remote restore process initiated for user {} from backup directory: {} to restore directory: {}", userId, backupDirectory, restoreDirectory);
        return "Remote backup restored.";
    }

    @DeleteMapping("/delete")
    public String deleteBackup(@RequestParam String backupDirectory, @RequestParam String userId, @RequestParam boolean isAws) {
        remoteBackupService.deleteBackup(backupDirectory, userId, isAws);
        logger.info("Remote backup deleted for directory user: {}, {}", backupDirectory, userId);
        return "Remote backup deleted.";
    }

    @PostMapping("/schedule")
    public String scheduleBackup(@RequestParam String sourceDirectory, @RequestParam String userId, @RequestParam int interval, @RequestParam boolean isAws) {
        remoteBackupService.scheduleRegularBackup(sourceDirectory, userId, interval, isAws);
        logger.info("Remote regular backup scheduled for user {} in source directory: {} with interval: {} hours", userId, sourceDirectory, interval);
        return "Remote regular backup scheduled.";
    }
}
