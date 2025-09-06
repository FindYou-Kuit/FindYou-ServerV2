package com.kuit.findyou.global.external.constant;

import lombok.Getter;

@Getter
public enum ExternalExceptionMessage {
    OPENAI_CLIENT_EMPTY_RESPONSE("OpenAI Vision API 응답이 비어있습니다."),
    OPENAI_CLIENT_CALL_FAILED("OpenAI Vision API 호출 중 오류가 발생했습니다."),

    OPENAI_VALIDATOR_SPECIES_INVALID("유효하지 않은 축종입니다."),
    OPENAI_VALIDATOR_BREED_GROUP_EMPTY("해당 축종에 대한 품종 정보가 없습니다."),
    OPENAI_VALIDATOR_BREED_INVALID("유효하지 않은 품종입니다."),
    OPENAI_VALIDATOR_COLORS_INVALID("유효하지 않은 색상입니다."),
    OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK("값이 null 이거나 비어 있습니다.");

    private String value;

    ExternalExceptionMessage(String value) {
        this.value = value;
    }
}
