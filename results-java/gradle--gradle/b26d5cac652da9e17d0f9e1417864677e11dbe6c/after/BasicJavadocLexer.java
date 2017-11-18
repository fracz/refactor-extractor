/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.build.docs.dsl.docbook;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Converts the main description of a javadoc comment into a stream of tokens.
 */
class BasicJavadocLexer implements JavadocLexer {
    private static final Pattern HTML_ELEMENT = Pattern.compile("<\\\\?.+?>");
    private static final Pattern END_ELEMENT_NAME = Pattern.compile("(/>)|>");
    private static final Pattern HTML_ENCODED_CHAR = Pattern.compile("&#\\d+;");
    private static final Pattern HTML_ENTITY = Pattern.compile("&.+?;");
    private static final Pattern TAG = Pattern.compile("(?s)\\{@.+?\\}");
    private static final Pattern END_TAG_NAME = Pattern.compile("(?s)\\s|}");
    private static final Pattern WHITESPACE_WITH_EOL = Pattern.compile("(?s)\\s+");
    private static final Map<String, String> ENTITIES = new HashMap<String, String>();

    static {
        ENTITIES.put("amp", "&");
        ENTITIES.put("lt", "<");
        ENTITIES.put("gt", ">");
    }

    private final JavadocScanner scanner;
    private TokenType tokenType;
    private String value;
    private String inlineTag;
    private boolean implicitEnd;

    BasicJavadocLexer(JavadocScanner scanner) {
        this.scanner = scanner;
    }

    public Token getToken() {
        return new Token(tokenType, value);
    }

    public void pushText(String rawCommentText) {
        scanner.pushText(rawCommentText);
    }

    public boolean next() {
        if (implicitEnd) {
            tokenType = TokenType.End;
            implicitEnd = false;
            return true;
        }

        if (scanner.isEmpty()) {
            tokenType = null;
            value = null;
            return false;
        }

        scanner.mark();
        while (!scanner.isEmpty()) {
            if (inlineTag == null && scanner.lookingAt(HTML_ELEMENT)) {
                break;
            }
            if (inlineTag == null && scanner.lookingAt(HTML_ENCODED_CHAR)) {
                break;
            }
            if (inlineTag == null && scanner.lookingAt(HTML_ENTITY)) {
                break;
            }
            if (scanner.lookingAt(TAG)) {
                break;
            }
            if (inlineTag != null && scanner.lookingAt('}')) {
                break;
            }
            scanner.next();
        }

        String region = scanner.region();
        if (region.length() > 0) {
            tokenType = TokenType.Text;
            value = region;
            return true;
        }

        if (inlineTag == null && scanner.lookingAt(HTML_ELEMENT)) {
            scanner.next();
            tokenType = TokenType.StartElement;
            if (scanner.lookingAt('/')) {
                tokenType = TokenType.End;
                scanner.next();
            }
            scanner.mark();
            scanner.find(END_ELEMENT_NAME);
            if (scanner.lookingAt('/')) {
                implicitEnd = true;
            }
            value = scanner.region().toLowerCase();
            scanner.skip(END_ELEMENT_NAME);
            return true;
        }

        if (inlineTag == null && scanner.lookingAt(HTML_ENCODED_CHAR)) {
            scanner.next(2);
            scanner.mark();
            scanner.find(';');
            tokenType = TokenType.Text;
            value = new String(new char[]{(char) Integer.parseInt(scanner.region())});
            scanner.next();
            return true;
        }

        if (inlineTag == null && scanner.lookingAt(HTML_ENTITY)) {
            scanner.next();
            scanner.mark();
            scanner.find(';');
            tokenType = TokenType.Text;
            value = ENTITIES.get(scanner.region().toLowerCase());
            scanner.next();
            return true;
        }

        if (scanner.lookingAt(TAG)) {
            scanner.next(2);
            scanner.mark();
            scanner.find(END_TAG_NAME);
            tokenType = TokenType.StartTag;
            value = scanner.region();
            inlineTag = value;
            scanner.skip(WHITESPACE_WITH_EOL);
            return true;
        }

        if (inlineTag != null && scanner.lookingAt('}')) {
            tokenType = TokenType.End;
            value = inlineTag;
            inlineTag = null;
            scanner.next();
            return true;
        }

        throw new IllegalStateException();
    }
}