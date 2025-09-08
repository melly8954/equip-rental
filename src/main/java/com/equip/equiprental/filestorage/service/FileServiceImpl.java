package com.equip.equiprental.filestorage.service;

import com.equip.equiprental.common.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileStorageStrategy fileStorageStrategy;
    private final FileProperties fileProperties;

    @Override
    public List<String> saveFiles(List<MultipartFile> files, String typeKey) {
        try {
            List<String> savedFilenames = storeFile(files, typeKey);
            return generateAccessUrl(savedFilenames, typeKey);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }
    }

    private List<String> storeFile(List<MultipartFile> files, String typeKey) throws IOException {
        return fileStorageStrategy.store(files, typeKey);
    }

    private List<String> generateAccessUrl(List<String> savedFilenames, String typeKey) {
        String baseUrl = fileProperties.getAccessUrlBase().replaceAll("/+$", ""); // 끝의 슬래시 모두 제거
        return savedFilenames.stream()
                .map(name -> String.format("%s/%s/%s", baseUrl, typeKey, name))
                .toList();
    }
}
