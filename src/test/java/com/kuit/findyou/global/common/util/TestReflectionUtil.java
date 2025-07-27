package com.kuit.findyou.global.common.util;

import java.lang.reflect.Field;

import static org.hibernate.internal.util.ReflectHelper.findField;

public class TestReflectionUtil {

    /**
     * 대상 객체의 private 필드에 리플렉션을 사용해 값을 주입
     *
     * @param target    필드가 존재하는 객체
     * @param fieldName 주입할 필드명
     * @param value     주입할 값
     */
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("리플렉션 주입 실패: " + fieldName, e);
        }
    }
}
