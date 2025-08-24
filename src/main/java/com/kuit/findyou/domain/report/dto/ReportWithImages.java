package com.kuit.findyou.domain.report.dto;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.report.model.Report;

import java.util.List;

public record ReportWithImages<T extends Report>(T report, List<ReportImage> images) {
}
