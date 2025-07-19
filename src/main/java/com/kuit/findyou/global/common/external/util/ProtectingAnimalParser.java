package com.kuit.findyou.global.common.external.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ProtectingAnimalItemParser {

    private static final Pattern KIND_PATTERN = Pattern.compile("^\\[(.+?)]\\s*(.+)$");

    
}
