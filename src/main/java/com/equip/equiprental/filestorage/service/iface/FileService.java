package com.equip.equiprental.filestorage.service.iface;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    List<String> saveFiles(List<MultipartFile> files, String typeKey);
    void deleteFile(String filePath, String typeKey);
}
