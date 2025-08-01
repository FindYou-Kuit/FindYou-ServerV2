package com.kuit.findyou.domain.report.util;

import java.math.BigDecimal;

public class ReportFormatUtil {

    private static final String UNKNOWN = "미상";
    private static final BigDecimal DEFAULT_COORDINATE = BigDecimal.valueOf(0.0);

    public static String formatAge(String age) {
        return age == null || UNKNOWN.equals(age) ? UNKNOWN : age + "살";
    }

    public static String formatWeight(String weight) {
        return weight == null || UNKNOWN.equals(weight) ? UNKNOWN : weight + "kg";
    }

    public static Double formatCoordinate(BigDecimal coordinate) {
        if (coordinate == null || coordinate.compareTo(DEFAULT_COORDINATE) == 0) {
            return null;
        }
        return coordinate.doubleValue();
    }
}
