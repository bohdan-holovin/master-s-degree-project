package com.holovin.diploma.client;
import com.holovin.diploma.model.BackupInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@FeignClient(name = "file-server", url = "http://localhost:8081")
public interface FileFeignClient {

    @PostMapping(value = "/remote/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file, @RequestParam String userId, @RequestParam String backupDirPath);

    @GetMapping("/remote/download")
    ResponseEntity<ByteArrayResource> downloadFile(@RequestParam String userId, @RequestParam String backupDirPath);

    @GetMapping("/remote/delete")
    ResponseEntity<String> deleteFile(@RequestParam String userId, @RequestParam String backupDirPath);

    @GetMapping("/remote/getBackupInfo")
    ResponseEntity<List<BackupInfo>> getBackupInfo(@RequestParam String userId);

    //////////////////////////////

    @PostMapping(value = "/aws/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadFileAws(@RequestPart("file") MultipartFile file, @RequestParam String userId, @RequestParam String backupDirPath);

    @GetMapping("/aws/download")
    ResponseEntity<ByteArrayResource> downloadFileAws(@RequestParam String userId, @RequestParam String backupDirPath);

    @GetMapping("/aws/delete")
    ResponseEntity<String> deleteFileAws(@RequestParam String userId, @RequestParam String backupDirPath);

    @GetMapping("/aws/getBackupInfo")
    ResponseEntity<List<BackupInfo>> getBackupInfoAws(@RequestParam String userId);
}
