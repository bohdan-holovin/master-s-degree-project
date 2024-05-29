package com.holovin.diploma.service;

import com.holovin.diploma.client.FileFeignClient;
import com.holovin.diploma.model.BackupInfo;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RemoteBackupService {

    private final FileFeignClient fileFeignClient;

    private static final Logger logger = LoggerFactory.getLogger(RemoteBackupService.class);

    private static final String BACKUP_DIRECTORY = "C:\\Projects\\Diploma\\diploma\\backup/";

    private static final String BACKUP_ZIP = "C:\\Projects\\Diploma\\diploma\\zip_backup/";

    @Autowired
    public RemoteBackupService(FileFeignClient fileFeignClient) {
        this.fileFeignClient = fileFeignClient;
    }

    public void createBackup(String sourceDirectory, String userId, boolean isAws) {
        File sourceDir = new File(sourceDirectory);

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid source directory");
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nameBackupTimestamp = "backup_" + timestamp;
        String backupDirPath = BACKUP_DIRECTORY + userId + "/" + nameBackupTimestamp + "/";
        File backupDir = new File(backupDirPath);

        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                throw new RuntimeException("Failed to create backup directory");
            }
        }

        try {
            var archiveZip = new ZipFile("zip_backup/" + nameBackupTimestamp + ".zip");
            archiveZip.addFiles(List.of(Objects.requireNonNull(sourceDir.listFiles())));
            var file = archiveZip.getFile();

            FileItem fileItem = new DiskFileItem("file", MediaType.MULTIPART_FORM_DATA_VALUE,
                    true, file.getName(), (int) file.length(), file.getParentFile());

            try {
                IOUtils.copy(new FileInputStream(file), fileItem.getOutputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
            if (isAws) {
                fileFeignClient.uploadFileAws(multipartFile, userId, nameBackupTimestamp);
            } else {
                fileFeignClient.uploadFile(multipartFile, userId, nameBackupTimestamp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Remote backup created successfully for user {} in {}", userId, backupDirPath);
    }

    public void restoreBackup(String backupDirectory, String restoreDirectory, String userId, boolean isAws) throws IOException {
        String nameBackupTimestamp = "backup_" + backupDirectory;

        ResponseEntity<ByteArrayResource> byteArrayResourceResponseEntity = null;
        if (isAws) {
            byteArrayResourceResponseEntity = fileFeignClient.downloadFileAws(userId, nameBackupTimestamp);
        } else {
            byteArrayResourceResponseEntity = fileFeignClient.downloadFile(userId, nameBackupTimestamp);
        }

        if (byteArrayResourceResponseEntity.getStatusCode().is2xxSuccessful()) {

            try {

                String zipFileName = BACKUP_ZIP + nameBackupTimestamp + ".zip";
                byte[] byteArray = Objects.requireNonNull(byteArrayResourceResponseEntity.getBody()).getByteArray();
                Path tempFile = Files.createTempFile(Paths.get(BACKUP_ZIP), nameBackupTimestamp, ".zip");
                Files.write(tempFile, byteArray, StandardOpenOption.CREATE);


                var archiveZip = new ZipFile(zipFileName);
                archiveZip.extractAll(restoreDirectory);
                logger.info("Remote restore successfully for user {} in {}", userId, restoreDirectory);

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            throw new InternalError();
        }


        logger.info("Backup restored successfully for user {} to {}", userId, restoreDirectory);
    }

    public List<BackupInfo> getBackupInfo(String userId, boolean isAws) {
        ResponseEntity<List<BackupInfo>> backupInfo = null;
        if (isAws) {
            backupInfo = fileFeignClient.getBackupInfoAws(userId);
        } else {
            backupInfo = fileFeignClient.getBackupInfo(userId);
        }
        if (backupInfo.getStatusCode().isError())
            throw new InternalError();
        return backupInfo.getBody();
    }

    public void deleteBackup(String timestamp, String userId, boolean isAws) {
        String nameBackupTimestamp = "backup_" + timestamp;
        ResponseEntity<String> stringResponseEntity = null;
        if (isAws) {
            stringResponseEntity = fileFeignClient.deleteFileAws(userId, nameBackupTimestamp);
        } else {
            stringResponseEntity = fileFeignClient.deleteFile(userId, nameBackupTimestamp);
        }
        if (stringResponseEntity.getStatusCode().isError()) {
            throw new InternalError();
        }

        logger.info("Backup remote deleted successfully: {}", nameBackupTimestamp);
    }

    public void scheduleRegularBackup(String sourceDirectory, String userId, long delayInHours, boolean isAws) {
        if (sourceDirectory == null || userId == null) {
            throw new IllegalArgumentException("sourceDirectory and userId cannot be null");
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> createBackup(sourceDirectory, userId, isAws), 0, delayInHours, TimeUnit.HOURS);
    }
}
