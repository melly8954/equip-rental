package com.equip.equiprental.filestorage.service;

import com.equip.equiprental.common.config.FileProperties;
import com.equip.equiprental.filestorage.domain.StoredFile;
import com.equip.equiprental.filestorage.service.iface.FileService;
import com.equip.equiprental.filestorage.service.iface.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileStorageStrategy fileStorageStrategy;
    private final FileProperties fileProperties;

    @Override
    public List<String> saveFiles(List<MultipartFile> files, String typeKey) {
        try {
            List<StoredFile> savedFilenames = storeFile(files, typeKey);
            return generateAccessUrl(savedFilenames, typeKey);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }
    }

    // 삭제 기능
    @Override
    public void deleteFile(String filePath, String typeKey) {
        if (filePath == null || filePath.isBlank()) return;

        try {
            // URL에서 base URL 제거 (예: http://localhost:8080/files/board/ 제거)
            String relativePath = filePath.replaceFirst("^.*/files/", "");

            // URL 디코딩 (한글/공백 복원)
            relativePath = java.net.URLDecoder.decode(relativePath, StandardCharsets.UTF_8);

            // typeKey 제거 (중복 방지)
            if (relativePath.startsWith(typeKey + "/")) {
                relativePath = relativePath.substring(typeKey.length() + 1); // +1은 '/' 제거
            }

            // 실제 파일 경로 계산
            String baseStoragePath = fileProperties.getStoragePath();
            Path path = Paths.get(baseStoragePath, relativePath);

            // 4. 삭제
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("삭제 성공: " + path);
            } else {
                System.out.println("삭제할 파일 없음: " + path);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패: " + filePath, e);
        }
    }

    private List<StoredFile> storeFile(List<MultipartFile> files, String typeKey) throws IOException {
        return fileStorageStrategy.store(files, typeKey);
    }

    private List<String> generateAccessUrl(List<StoredFile> savedFilenames, String typeKey) {
        String baseUrl = fileProperties.getAccessUrlBase().replaceAll("/+$", ""); // 끝의 슬래시 모두 제거
        return savedFilenames.stream()
                .map(file -> String.format("%s/%s/%s", baseUrl, typeKey.replace("_","/"), file.getLocalFileName()))
                .toList();
    }
}
