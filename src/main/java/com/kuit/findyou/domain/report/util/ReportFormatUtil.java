package com.kuit.findyou.domain.report.util;

public class ReportFormatUtil {

    private static final String UNKNOWN = "미상";

    public static String formatAge(String age) {
        return UNKNOWN.equals(age) ? UNKNOWN : age + "살";
    }

    public static String formatWeight(String weight) {
        return UNKNOWN.equals(weight) ? UNKNOWN : weight + "kg";
    }
}
