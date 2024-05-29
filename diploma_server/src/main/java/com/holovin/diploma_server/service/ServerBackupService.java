package com.holovin.diploma_server.service;

import com.holovin.diploma_server.model.BackupInfo;
import net.lingala.zip4j.ZipFile;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ServerBackupService {

    private static final Logger logger = LoggerFactory.getLogger(ServerBackupService.class);

    private static final String BACKUP_DIRECTORY = "C:\\Projects\\Diploma\\diploma_server\\backup/";
    private static final String BACKUP_ZIP_DIRECTORY = "C:\\Projects\\Diploma\\diploma_server\\backup_zip/";

    public void storeBackup(MultipartFile fileZip, String userId, String nameBackupTimestamp) {
        String backupZipPath = BACKUP_ZIP_DIRECTORY + userId + "/" + nameBackupTimestamp + ".zip";
        var path = new File(backupZipPath);

        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw new RuntimeException("Failed to create backup directory");
            }
        }
        try {
            // save file as .zip
            var backupZipFile = new File(backupZipPath);
            backupZipFile.createNewFile();
            fileZip.transferTo(backupZipFile);

            // extract zip file
            var zipFile = new ZipFile(backupZipFile);
            String backupDirPath = BACKUP_DIRECTORY + userId + "/" + nameBackupTimestamp + "/";
            zipFile.extractAll(backupDirPath);
            logger.info("Backup created successfully for user {} in {}", userId, backupDirPath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] download(String userId, String nameBackupTimestamp) throws IOException {
        String sourceDirectory = BACKUP_DIRECTORY + userId + "/" + nameBackupTimestamp + "/";
        File sourceDir = new File(sourceDirectory);

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid source directory");
        }

        try {
            var archiveZip = new ZipFile(BACKUP_ZIP_DIRECTORY + nameBackupTimestamp + ".zip");
            archiveZip.addFiles(List.of(Objects.requireNonNull(sourceDir.listFiles())));
            byte[] zipBytesFile = Files.readAllBytes(Path.of(archiveZip.getFile().getPath()));
            var file = archiveZip.getFile();

            FileItem fileItem = new DiskFileItem("file", MediaType.MULTIPART_FORM_DATA_VALUE,
                    true, file.getName(), (int) file.length(), file.getParentFile());

            try {
                IOUtils.copy(new FileInputStream(file), fileItem.getOutputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
            logger.info("Remote backup created successfully for user {} in {}", userId, sourceDir);
            return zipBytesFile;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void delete(String userId, String timestamp) {
        Path backupDir = Paths.get(BACKUP_DIRECTORY + userId + "/" + timestamp);

        if (!Files.exists(backupDir) || !Files.isDirectory(backupDir)) {
            throw new IllegalArgumentException("Invalid backup directory");
        }

        try {
            Files.walk(backupDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            logger.error("Error deleting backup directory: {}", backupDir, e);
            throw new RuntimeException("Error deleting backup directory", e);
        }

        logger.info("Backup deleted successfully: {}", backupDir);
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
                                backupInfo.setStorage("Remote server");
                            }
                        }
                    }
                }
            }
        }
        List<BackupInfo> collect = backupInfoList.stream().filter(it -> it.getUser().equals(userId)).collect(Collectors.toList());
        return collect;
    }
}
