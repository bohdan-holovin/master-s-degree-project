package com.holovin.diploma.service;

import com.holovin.diploma.model.BackupInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LocalBackupService {
    private static final Logger logger = LoggerFactory.getLogger(LocalBackupService.class);

    private static final String BACKUP_DIRECTORY = "backup/";

    public void createBackup(String sourceDirectory, String userId) {
        File sourceDir = new File(sourceDirectory);

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid source directory");
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupDirPath = BACKUP_DIRECTORY + userId + "/" + "backup_" + timestamp + "/";
        File backupDir = new File(backupDirPath);

        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                throw new RuntimeException("Failed to create backup directory");
            }
        }

        try {
            Files.walk(Paths.get(sourceDirectory))
                    .forEach(source -> {
                        try {
                            Path destination = Paths.get(backupDirPath, sourceDir.toPath().relativize(source).toString());
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            logger.error("Error copying file during backup for user {}", userId, e);
                            throw new RuntimeException("Error copying file during backup", e);
                        }
                    });
        } catch (IOException e) {
            logger.error("Error accessing files during backup for user {}", userId, e);
            throw new RuntimeException("Error accessing files during backup", e);
        }

        logger.info("Backup created successfully for user {} in {}", userId, backupDirPath);
    }

    public void restoreBackup(String backupDirectory, String restoreDirectory, String userId) {
        String backupDirectoryDir = BACKUP_DIRECTORY + userId + "/" + "backup_" + backupDirectory;
        Path backupDir = Paths.get(backupDirectoryDir);

        if (!Files.exists(backupDir) || !Files.isDirectory(backupDir)) {
            throw new IllegalArgumentException("Invalid backup directory");
        }

        Path restoreDir = Paths.get(restoreDirectory, userId);

        if (!Files.exists(restoreDir)) {
            try {
                Files.createDirectories(restoreDir);
                logger.info("Restore directory created for user {} at {}", userId, restoreDir);
            } catch (IOException e) {
                logger.error("Failed to create restore directory for user {}: {}", userId, restoreDir, e);
                throw new RuntimeException("Failed to create restore directory", e);
            }
        }

        if (Files.exists(restoreDir)) {
            try {
                // Видаліть попередні файли у каталозі відновлення
                Files.walk(restoreDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                logger.error("Failed to clear restore directory for user {}: {}", userId, restoreDir, e);
                throw new RuntimeException("Failed to clear restore directory", e);
            }
        }

        try {
            Files.walk(backupDir)
                    .forEach(source -> {
                        try {
                            Path destination = Paths.get(restoreDir.toString(), backupDir.relativize(source).toString());
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            logger.error("Error copying file during restore for user {}", userId, e);
                        }
                    });
        } catch (IOException e) {
            logger.error("Error accessing files during restore for user {}", userId, e);
            throw new RuntimeException("Error accessing files during restore", e);
        }

        logger.info("Backup restored successfully for user {} to {}", userId, restoreDirectory);
    }

    public String getBackupStatus() {
        File backupDirectory = new File(BACKUP_DIRECTORY);
        if (backupDirectory.exists() && backupDirectory.isDirectory()) {
            File[] userDirectories = backupDirectory.listFiles();
            if (userDirectories != null) {
                return "Backup is available";
            }
        }
        return "No backups available.";
    }

    public List<BackupInfo> getBackupInfo(String userId) {
        List<BackupInfo> backupInfoList = new ArrayList<>();
        File backupDirectory = new File(BACKUP_DIRECTORY);
        if (backupDirectory.exists() && backupDirectory.isDirectory()) {
            File[] userDirectories = backupDirectory.listFiles();
            if (userDirectories != null) {
                for (File userDir : userDirectories) {
                    if (userDir.isDirectory()) {
                        File[] backups = userDir.listFiles();
                        if (backups != null) {
                            for (File backup : backups) {
                                BackupInfo backupInfo = new BackupInfo();
                                backupInfo.setId(backup.getName());
                                backupInfo.setUser(userDir.getName());
                                backupInfo.setTimestamp(backup.getName().substring(7)); // Assuming backup_YYYYMMDD_HHmmss
                                backupInfo.setStatus("Available");
                                backupInfoList.add(backupInfo);
                                backupInfo.setStorage("Local");
                            }
                        }
                    }
                }
            }
        }
        List<BackupInfo> collect = backupInfoList.stream().filter(it -> it.getUser().equals(userId)).collect(Collectors.toList());
        return collect;
    }

    public void deleteBackup(String backupDirectory, String userId) {
        Path backupDir = Paths.get(BACKUP_DIRECTORY + userId + "/" + "backup_" + backupDirectory);

        if (!Files.exists(backupDir) || !Files.isDirectory(backupDir)) {
            throw new IllegalArgumentException("Invalid backup directory");
        }

        try {
            Files.walk(backupDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            logger.error("Error deleting backup directory: {}", backupDirectory, e);
            throw new RuntimeException("Error deleting backup directory", e);
        }

        logger.info("Backup deleted successfully: {}", backupDirectory);
    }

    public void scheduleRegularBackup(String sourceDirectory, String userId, long delayInHours) {
        if (sourceDirectory == null || userId == null) {
            throw new IllegalArgumentException("sourceDirectory and userId cannot be null");
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> createBackup(sourceDirectory, userId), 0, delayInHours, TimeUnit.HOURS);
    }
}
