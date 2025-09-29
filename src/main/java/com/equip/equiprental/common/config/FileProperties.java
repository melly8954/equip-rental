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
        if (parts.length == 0) return storagePath;

        // 첫 번째 부분은 directories 매핑 사용
        String dir1 = directories.getOrDefault(parts[0], parts[0]);

        // 나머지 부분을 하위 디렉토리로 사용
        String[] subDirs = (parts.length > 1) ? java.util.Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        // dir1 + subDirs를 합쳐서 가변인자로 전달
        String[] fullDirs = new String[subDirs.length + 1];
        fullDirs[0] = dir1;
        System.arraycopy(subDirs, 0, fullDirs, 1, subDirs.length);

        return Paths.get(storagePath, fullDirs).toString();
    }
}
