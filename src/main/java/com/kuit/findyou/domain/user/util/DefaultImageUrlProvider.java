package com.kuit.findyou.domain.user.util;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "findyou.default-images")
@Setter
public class DefaultImageUrlProvider {
    private Map<String, String> defaultImageUrlMap;

    @PostConstruct
    public void validate() {
        if (defaultImageUrlMap == null || defaultImageUrlMap.isEmpty()) {
            throw new IllegalStateException("defaultImageUrlMap 설정이 비어있습니다. application.yml을 확인하세요!");
        }
    }

    public String getImageUrl(String name) {
        String imageUrl = defaultImageUrlMap.get(name);
        if (imageUrl == null){
            throw new IllegalArgumentException("존재하지 않는 키값입니다");
        }
        return imageUrl;
    }

    public boolean containsKey(String name){
        return defaultImageUrlMap.containsKey(name);
    }
}
