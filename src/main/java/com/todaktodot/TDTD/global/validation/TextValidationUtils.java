package com.todaktodot.TDTD.global.validation;

import java.util.regex.Pattern;

final class TextValidationUtils {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is).*<\\s*/?\\s*[a-z][^>]*>.*");
    private static final Pattern DISALLOWED_CONTROL_CHAR_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");

    private TextValidationUtils() {
    }

    static boolean containsHtmlTagLikeText(String value) {
        return value != null && HTML_TAG_PATTERN.matcher(value).matches();
    }

    static boolean containsDisallowedControlCharacter(String value) {
        return value != null && DISALLOWED_CONTROL_CHAR_PATTERN.matcher(value).find();
    }
}
