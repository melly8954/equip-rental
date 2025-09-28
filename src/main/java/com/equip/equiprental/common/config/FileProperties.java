package com.equip.equiprental.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileProperties {
    private String accessUrlBase;
    private String storagePath; // 최상위 저장소 루트
    private Map<String, String> directories; // 서브 디렉토리 구분 (equipment, rental 등)

    public String getFullPath(String typeKey) {
        String[] parts = typeKey.split("_");
        String dir1 = directories.getOrDefault(parts[0], parts[0]);
        String dir2 = (parts.length > 1) ? parts[1] : ""; // 안전하게 처리
        return Paths.get(storagePath, dir1, dir2).toString();
    }
}
