package com.equip.equiprental.equipment.util;

import java.util.HashMap;
import java.util.Map;

public class EquipmentCodeMap {
    public static final Map<String, String> categoryCodeMap = new HashMap<>();
    public static final Map<String, String> subCategoryCodeMap = new HashMap<>();

    static {
        // 카테고리 코드
        categoryCodeMap.put("OFFICE_SUPPLIES", "OS");
        categoryCodeMap.put("ELECTRONICS", "EL");
        categoryCodeMap.put("FURNITURE", "FR");
        categoryCodeMap.put("TOOLS", "TL");
        categoryCodeMap.put("SAFETY_EQUIPMENT", "SE");

        // 서브카테고리 코드
        subCategoryCodeMap.put("문서 파쇄기", "DS");
        subCategoryCodeMap.put("라벨프린터", "LP");
        subCategoryCodeMap.put("프로젝트 보드", "PB");
        subCategoryCodeMap.put("노트북", "NB");
        subCategoryCodeMap.put("태블릿", "TB");
        subCategoryCodeMap.put("프로젝터", "PR");
        subCategoryCodeMap.put("모니터", "MN");
        subCategoryCodeMap.put("프린터", "PR");
        subCategoryCodeMap.put("카메라/캠코더", "CM");
        subCategoryCodeMap.put("오디오장비(스피커/마이크)", "AD");
        subCategoryCodeMap.put("외장저장장치(SSD/HDD)", "ST");
        subCategoryCodeMap.put("사무용 의자", "CH");
        subCategoryCodeMap.put("책상", "DS");
        subCategoryCodeMap.put("서랍장/캐비닛", "CA");
        subCategoryCodeMap.put("이동식 파티션", "PT");
        subCategoryCodeMap.put("화이트보드", "WB");
        subCategoryCodeMap.put("전동공구(드릴, 그라인더)", "DR");
        subCategoryCodeMap.put("수공구(망치, 드라이버)", "HD");
        subCategoryCodeMap.put("측정도구(레이저측정기, 콤파스)", "MS");
        subCategoryCodeMap.put("납땜장비", "SD");
        subCategoryCodeMap.put("안전모", "HE");
        subCategoryCodeMap.put("안전화", "SH");
        subCategoryCodeMap.put("보호안경/귀마개", "GL");
        subCategoryCodeMap.put("방진마스크", "MK");
        subCategoryCodeMap.put("소화기/응급키트", "FX");
    }
}
