package com.kuit.findyou.global.external.constant;

import lombok.Getter;

@Getter
public enum ExternalExceptionMessage {
    OPENAI_CLIENT_EMPTY_RESPONSE("OpenAI Vision API 응답이 비어있습니다."),
    OPENAI_CLIENT_CALL_FAILED("OpenAI Vision API 호출 중 오류가 발생했습니다."),

    OPENAI_PARSER_SPECIES_INVALID("유효하지 않은 축종입니다."),
    OPENAI_PARSER_BREED_GROUP_EMPTY("해당 축종에 대한 품종 정보가 없습니다."),
    OPENAI_PARSER_BREED_INVALID("유효하지 않은 품종입니다."),
    OPENAI_PARSER_COLORS_EMPTY("유효한 색상이 없습니다."),
    OPENAI_PARSER_INPUT_NULL_OR_BLANK("입력이 null 이거나 비어 있습니다."),
    OPENAI_PARSER_NO_COMMA_DELIMITER("입력에 쉼표(,) 구분자가 없습니다. 잘못된 포맷입니다"),
    OPENAI_PARSER_PARTS_TOO_FEW("입력 항목이 부족합니다. 최소 3개(축종, 품종, 색상)가 필요합니다.");


    private String value;

    ExternalExceptionMessage(String value) {
        this.value = value;
    }
}
