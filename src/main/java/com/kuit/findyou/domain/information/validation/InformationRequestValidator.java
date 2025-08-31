package com.kuit.findyou.domain.information.validation;

import com.kuit.findyou.global.common.exception.CustomException;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

public class InformationRequestValidator {
    public static String nullIfBlank(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }

    public static Double parseDoubleOrNull(String raw) {
        String t = nullIfBlank(raw);
        if (t == null) return null;
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            throw new CustomException(INVALID_COORDINATE);
        }
    }


    public static Long validateCursor(Long lastId) {
        Long cursor = (lastId == null || lastId == 0L) ? null : lastId;
        if (cursor != null && cursor < 0) {
            throw new CustomException(INVALID_CURSOR);
        }
        return cursor;
    }

    public static void validateGeoOrFilter(Double latVal, Double lonVal,
                                           String districtNorm) {
        boolean hasGeo = (latVal != null && lonVal != null);
        boolean hasFilter = (districtNorm != null);
        if (!hasGeo && !hasFilter) {
            throw new CustomException(GEO_OR_FILTER_REQUIRED);
        }
    }

    public static void validateLatLngPair(Double latVal, Double lonVal) {
        if ((latVal == null) ^ (lonVal == null)) {
            throw new CustomException(LAT_LONG_PAIR_REQUIRED);
        }
    }

    public static void validatePageSize(int size) {
        if (size <= 0) throw new CustomException(INVALID_SIZE);
    }
}
