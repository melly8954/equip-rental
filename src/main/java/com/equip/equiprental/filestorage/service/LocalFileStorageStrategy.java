package com.equip.equiprental.filestorage.service;

import com.equip.equiprental.common.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalFileStorageStrategy implements FileStorageStrategy {

    private final FileProperties fileProperties;

    @Override
    public List<String> store(List<MultipartFile> files, String typeKey) {
        List<String> savedNames = new ArrayList<>();

        String directoryPath = fileProperties.getFullPath(typeKey);

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) continue;

                String originalFilename = file.getOriginalFilename();
                String extension = "";
                int dotIndex = originalFilename != null ? originalFilename.lastIndexOf('.') : -1;
                if (dotIndex > 0) extension = originalFilename.substring(dotIndex);

                String uniqueName = UUID.randomUUID().toString() + extension;

                Path targetPath = Paths.get(directoryPath, uniqueName);
                Files.createDirectories(targetPath.getParent());
                Files.write(targetPath, file.getBytes());

                savedNames.add(uniqueName);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 중 오류 발생", e);
            }
        }

        return savedNames;
    }

    @Override
    public Resource load(String path) {
        return null;
    }
}
