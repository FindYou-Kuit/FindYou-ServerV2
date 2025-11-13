package com.kuit.findyou.global.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VolunteerWorksByKeywordApiResponse {
    private Header header;
    private Body body;

    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        private List<Item> items;
        private String numOfRows;
        private String pageNo;
        private String totalCount;
    }

    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JacksonXmlProperty(localName = "actBeginTm")
        private String actBeginTm;

        @JacksonXmlProperty(localName = "actEndTm")
        private String actEndTm;

        @JacksonXmlProperty(localName = "actPlace")
        private String actPlace;

        @JacksonXmlProperty(localName = "nanmmbyNm")
        private String nanmmbyNm;

        @JacksonXmlProperty(localName = "noticeBgnde")
        private String noticeBgnde;    // "yyyyMMdd"

        @JacksonXmlProperty(localName = "noticeEndde")
        private String noticeEndde;    // "yyyyMMdd"

        @JacksonXmlProperty(localName = "progrmBgnde")
        private String progrmBgnde;    // "yyyyMMdd"

        @JacksonXmlProperty(localName = "progrmEndde")
        private String progrmEndde;    // "yyyyMMdd"

        @JacksonXmlProperty(localName = "progrmRegistNo")
        private String progrmRegistNo;

        @JacksonXmlProperty(localName = "progrmSttusSe")
        private String progrmSttusSe;  // 코드값

        @JacksonXmlProperty(localName = "url")
        private String url;
    }
}
