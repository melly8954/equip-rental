package com.equip.equiprental.filestorage.service;

import com.equip.equiprental.common.config.FileProperties;
import com.equip.equiprental.filestorage.domain.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
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

        // URL에서 파일명만 추출
        String fileName = Paths.get(URI.create(filePath).getPath()).getFileName().toString();

        // FileProperties를 사용해 실제 저장 경로 계산
        String fullDir = fileProperties.getFullPath(typeKey);
        Path path = Paths.get(fullDir, fileName);

        // 실제 파일 삭제
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("삭제 성공: " + path);
            } else {
                System.out.println("삭제할 파일 없음: " + path);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패: " + path, e);
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
