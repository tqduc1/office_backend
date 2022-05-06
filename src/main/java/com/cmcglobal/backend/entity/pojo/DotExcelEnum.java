package com.cmcglobal.backend.entity.pojo;

import lombok.Getter;

@Getter
public enum DotExcelEnum {
    STT("STT"),
    GROUP("KHỐI"),
    DEPARTMENT("BỘ PHẬN"),
    BUILDING("TOÀ"),
    FLOOR("TẦNG"),
    REGISTRATION_NUMBER("SỐ LƯỢNG ĐĂNG KÝ"),
    ACTUAL_NUMBER_OF_SEATS("SỐ LƯỢNG NGỒI THỰC TẾ");

    private final String type;

    DotExcelEnum(String type) {
        this.type = type;
    }
}
