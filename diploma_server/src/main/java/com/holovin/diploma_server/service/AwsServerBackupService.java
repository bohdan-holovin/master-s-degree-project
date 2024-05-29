package com.holovin.diploma_server.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.holovin.diploma_server.model.BackupInfo;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AwsServerBackupService {
    private static final Logger logger = LoggerFactory.getLogger(AwsServerBackupService.class);
    private String awsS3Bucket = "some";
    private String awsS3Key = "some";
    private AmazonS3 s3Client;

    private static final String BACKUP_TEMP_DIRECTORY = "C:\\Projects\\Diploma\\diploma_server\\backup_temp/";
    private static final String BACKUP_ZIP_DIRECTORY = "C:\\Projects\\Diploma\\diploma_server\\backup_zip/";

    public AwsServerBackupService() {
        // Initialize S3 client

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsS3Key, "zdhSy7cf/VkL3e0cUPPx2pS78iHdAFmlJwUiI/LQ");
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.EU_NORTH_1)
                .build();
    }

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

            uploadBackupToS3(backupZipFile.getAbsolutePath(), userId + "/" + nameBackupTimestamp);
            logger.info("AWS Backup created successfully for user {} in {}", userId, userId + "/" + nameBackupTimestamp);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] download(String userId, String nameBackupTimestamp) throws IOException {
        String sourceDirectory = BACKUP_TEMP_DIRECTORY + userId + "/" + nameBackupTimestamp + "/";
        File sourceDir = new File(sourceDirectory);

        if (!sourceDir.exists()) {
            if (!sourceDir.mkdirs()) {
                throw new RuntimeException("Failed to create backup directory");
            }
        }

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid source directory");
        }

        downloadFromS3(userId + "/" + nameBackupTimestamp, BACKUP_TEMP_DIRECTORY + nameBackupTimestamp + ".zip");

        try {
            byte[] zipBytesFile = Files.readAllBytes(Path.of(BACKUP_TEMP_DIRECTORY + nameBackupTimestamp + ".zip"));

            logger.info("Remote backup created successfully for user {} in {}", userId, sourceDir);
            return zipBytesFile;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void downloadFromS3(String s3Key, String localPath) {
        try {
            S3Object s3object = s3Client.getObject(awsS3Bucket, s3Key);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            Files.copy(inputStream, Paths.get(localPath));
            inputStream.close();
            logger.info("Backup downloaded successfully from S3");
        } catch (AmazonS3Exception | IOException e) {
            logger.error("Error downloading from S3", e);
            throw new RuntimeException(e);
        }
    }

    public void delete(String userId, String timestamp) {
        String s3Key = userId + "/" + timestamp;
        deleteBackupFromS3(s3Key);
        logger.info("Backup deleted successfully: {}", s3Key);
    }


    public void deleteBackupFromS3(String s3Key) {
        try {
            s3Client.deleteObject(awsS3Bucket, s3Key);
            logger.info("Backup deleted successfully from S3");
        } catch (AmazonS3Exception e) {
            logger.error("Error deleting from S3", e);
            throw new RuntimeException(e);
        }
    }

    public List<BackupInfo> getBackupInfo(String userId) {
        List<BackupInfo> backupInfos = new ArrayList<>();
        try {
            ListObjectsV2Result result = s3Client.listObjectsV2(awsS3Bucket);
            for (S3ObjectSummary summary : result.getObjectSummaries()) {
                String cleanedString = summary.getKey().replace("/", " ");
                String[] parts = cleanedString.split(" ");
                BackupInfo backupInfo = new BackupInfo();
                backupInfo.setId(summary.getKey());
                backupInfo.setUser(parts[0]);
                backupInfo.setTimestamp(parts[1].substring(7));
                backupInfo.setStatus("Available");
                backupInfo.setStorage("AWS S3");
                backupInfos.add(backupInfo);
            }
            logger.info("List of backups retrieved successfully from S3");
        } catch (AmazonS3Exception e) {
            logger.error("Error listing backups from S3", e);
            throw new RuntimeException(e);
        }
        return backupInfos.stream().filter(it -> it.getUser().equals(userId)).collect(Collectors.toList());
    }

    public void uploadBackupToS3(String localFilePath, String s3Key) {
        try {
            s3Client.putObject(new PutObjectRequest(awsS3Bucket, s3Key, new File(localFilePath)));
            logger.info("Backup uploaded successfully to S3");
        } catch (AmazonServiceException e) {
            logger.error("Error uploading to S3", e);
            throw new RuntimeException(e);
        }
    }
}
