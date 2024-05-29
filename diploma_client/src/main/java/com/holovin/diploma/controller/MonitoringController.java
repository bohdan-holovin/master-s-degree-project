package com.holovin.diploma.controller;

import com.holovin.diploma.model.BackupInfo;
import com.holovin.diploma.service.LocalBackupService;
import com.holovin.diploma.service.RemoteBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);

    private final LocalBackupService localBackupService;
    private final RemoteBackupService remoteBackupService;

    @Autowired
    public MonitoringController(LocalBackupService localBackupService, RemoteBackupService remoteBackupService) {
        this.localBackupService = localBackupService;
        this.remoteBackupService = remoteBackupService;
    }

    @GetMapping("/status")
    public ResponseEntity<String> getBackupStatus() {
        String status = localBackupService.getBackupStatus();
        logger.info("Get backup status");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/backupInfo")
    public ResponseEntity<List<BackupInfo>> getBackupInfo(@RequestParam String userId) {
        List<BackupInfo> backupInfoLocal = localBackupService.getBackupInfo(userId);
        List<BackupInfo> backupInfoRemote = remoteBackupService.getBackupInfo(userId, false);
        List<BackupInfo> backupInfoRemoteAws = remoteBackupService.getBackupInfo(userId, true);
        backupInfoLocal.addAll(backupInfoRemote);
        backupInfoLocal.addAll(backupInfoRemoteAws);
        logger.info("Get back info for user {}", userId);
        return ResponseEntity.ok(backupInfoLocal);
    }
}
