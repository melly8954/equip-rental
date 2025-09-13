package com.equip.equiprental.filestorage.service;

import com.equip.equiprental.common.config.FileProperties;
import com.equip.equiprental.filestorage.domain.StoredFile;
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
    public List<StoredFile> store(List<MultipartFile> files, String typeKey) {
        List<StoredFile> savedFiles = new ArrayList<>();

        String directoryPath = fileProperties.getFullPath(typeKey);

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) continue;

                String originalFilename = file.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();

                // 로컬 저장용 파일명: UUID_원본파일명
                String localFileName = uuid + "_" + originalFilename;

                Path targetPath = Paths.get(directoryPath, localFileName);
                Files.createDirectories(targetPath.getParent());
                Files.write(targetPath, file.getBytes());

                savedFiles.add(new StoredFile(uuid, localFileName, originalFilename));
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 중 오류 발생", e);
            }
        }

        return savedFiles;
    }

    @Override
    public Resource load(String path) {
        return null;
    }
}
