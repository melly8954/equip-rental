package com.equip.equiprental.filestorage.service.iface;

import com.equip.equiprental.filestorage.domain.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageStrategy {
    List<StoredFile> store(List<MultipartFile> files, String typeKey);
    Resource load(String path);
}
