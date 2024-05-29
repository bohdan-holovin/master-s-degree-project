package com.holovin.diploma_server.controller;

import com.holovin.diploma_server.service.AwsServerBackupService;
import com.holovin.diploma_server.service.ServerBackupService;
import com.holovin.diploma_server.model.BackupInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class FileServerController {

    private final ServerBackupService serverBackupService;
    private final AwsServerBackupService awsServerBackupService;

    private static final Logger logger = LoggerFactory.getLogger(FileServerController.class);

    @Autowired
    public FileServerController(ServerBackupService serverBackupService, AwsServerBackupService awsServerBackupService) {
        this.serverBackupService = serverBackupService;
        this.awsServerBackupService = awsServerBackupService;
    }

    @PostMapping("/remote/upload")
    public ResponseEntity<String> uploadFile(@RequestBody MultipartFile file, @RequestParam String userId, @RequestParam String backupDirPath) {
        serverBackupService.storeBackup(file, userId, backupDirPath);
        logger.info("Backup process initiated for user {} in source directory: {}", userId, backupDirPath);
        return ResponseEntity.ok("Backup uploaded successfully.");
    }

    @GetMapping("/remote/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam String userId, @RequestParam String backupDirPath) throws IOException {
        ByteArrayResource byteArrayResource = new ByteArrayResource(serverBackupService.download(userId, backupDirPath));
        logger.info("Backup download initiated for user {} in source directory: {}", userId, backupDirPath);
        return ResponseEntity.ok()
                .contentLength(byteArrayResource.contentLength())
                .body(byteArrayResource);
    }

    @GetMapping("/remote/delete")
    ResponseEntity<String> deleteFile(@RequestParam String userId, @RequestParam String backupDirPath) {
        serverBackupService.delete(userId, backupDirPath);
        logger.info("Backup delete initiated for user {} in source directory: {}", userId, backupDirPath);
        return ResponseEntity.ok("Delete ok");
    }

    @GetMapping("/remote/getBackupInfo")
    ResponseEntity<List<BackupInfo>> getBackupInfo(@RequestParam String userId) {
        List<BackupInfo> backupInfo = serverBackupService.getBackupInfo(userId);
        logger.info("Get backupInfo initiated for user {}", userId);
        return ResponseEntity.ok(backupInfo);
    }

    //////////////////////////////////////////////

    @PostMapping("/aws/upload")
    public ResponseEntity<String> uploadFileAws(@RequestBody MultipartFile file, @RequestParam String userId, @RequestParam String backupDirPath) {
        awsServerBackupService.storeBackup(file, userId, backupDirPath);
        logger.info("Aws Backup process initiated for user {} in source directory: {}", userId, backupDirPath);
        return ResponseEntity.ok("Backup uploaded successfully.");
    }

    @GetMapping("/aws/download")
    public ResponseEntity<ByteArrayResource> downloadFileAws(@RequestParam String userId, @RequestParam String backupDirPath) throws IOException {
        ByteArrayResource byteArrayResource = new ByteArrayResource(awsServerBackupService.download(userId, backupDirPath));
        logger.info("Aws Backup download initiated for user {} in source directory: {}", userId, backupDirPath);
        return ResponseEntity.ok()
                .contentLength(byteArrayResource.contentLength())
                .body(byteArrayResource);
    }

    @GetMapping("/aws/delete")
    ResponseEntity<String> deleteFileAws(@RequestParam String userId, @RequestParam String backupDirPath) {
        awsServerBackupService.delete(userId, backupDirPath);
        logger.info("Aws Backup delete initiated for user {} in source directory: {}", userId, backupDirPath);
        return ResponseEntity.ok("Delete ok");
    }

    @GetMapping("/aws/getBackupInfo")
    ResponseEntity<List<BackupInfo>> getBackupInfoAws(@RequestParam String userId) {
        List<BackupInfo> backupInfo = awsServerBackupService.getBackupInfo(userId);
        logger.info("Aws Get backupInfo initiated for user {}", userId);
        return ResponseEntity.ok(backupInfo);
    }
}
