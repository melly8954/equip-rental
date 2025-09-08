package com.equip.equiprental.filestorage.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageStrategy {
    List<String> store(List<MultipartFile> files, String typeKey);
    Resource load(String path);
}
