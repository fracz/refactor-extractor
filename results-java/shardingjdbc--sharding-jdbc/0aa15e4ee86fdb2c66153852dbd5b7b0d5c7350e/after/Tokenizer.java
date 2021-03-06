/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.sql.lexer;

import lombok.RequiredArgsConstructor;

/**
 * 处理词.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class Tokenizer {

    private final String input;

    private final Dictionary dictionary;

    private final int offset;

    int skipWhitespace() {
        int length = 0;
        while (CharTypes.isWhitespace(charAt(offset + length))) {
            length++;
        }
        return offset + length;
    }

    int skipComment() {
        if ('/' == charAt(offset) && '/' ==  charAt(offset + 1) || '-' == charAt(offset) && '-' == charAt(offset + 1)) {
            return skipSingleLineComment(2);
        } else if ('#' == charAt(offset)) {
            return skipSingleLineComment(1);
        } else if ('/' == charAt(offset) && '*' == charAt(offset + 1)) {
            return skipMultiLineComment();
        }
        return offset;
    }

    private int skipSingleLineComment(final int commentFlagLength) {
        int position = offset + commentFlagLength;
        int length = commentFlagLength;
        while (CharTypes.EOI != charAt(position) && '\n' != charAt(position)) {
            position++;
            length++;
        }
        return offset + length + 1;
    }

    private int skipMultiLineComment() {
        return untilCommentAndHintTerminateSign(2);
    }

    int skipHint() {
        return untilCommentAndHintTerminateSign(3);
    }

    private int untilCommentAndHintTerminateSign(final int beginSignLength) {
        int length = beginSignLength;
        int position = offset + length;
        while (!('*' == charAt(position) && '/' == charAt(position + 1))) {
            if (CharTypes.EOI == charAt(position)) {
                throw new UnterminatedSignException("*/");
            }
            position++;
            length++;
        }
        length += 2;
        return offset + length;
    }

    Token scanUntil(final char terminatedSign, final TokenType defaultTokenType) {
        int length = 2;
        int position = offset + 1;
        while (terminatedSign != charAt(++position)) {
            if (CharTypes.EOI == charAt(position)) {
                throw new UnterminatedSignException(terminatedSign);
            }
            length++;
        }
        length++;
        String literals = input.substring(offset, offset + length);
        return new Token(dictionary.getToken(literals, defaultTokenType), literals, offset + length);
    }

    Token scanVariable() {
        int length = 1;
        int position = offset;
        if ('@' == charAt(position + 1)) {
            position++;
            length++;
        }
        while (isVariableChar(charAt(++position))) {
            length++;
        }
        return new Token(Literals.VARIABLE, input.substring(offset, offset + length), offset + length);
    }

    private boolean isVariableChar(final char ch) {
        return isIdentifierChar(ch) || '.' == ch;
    }

    Token scanIdentifier() {
        if ('`' == charAt(offset)) {
            return scanUntil('`', Literals.IDENTIFIER);
        }
        int length = 1;
        int position = offset;
        while (isIdentifierChar(charAt(++position))) {
            length++;
        }
        String literals = input.substring(offset, offset + length);
        if (isAmbiguousIdentifier(literals)) {
            return new Token(processAmbiguousIdentifier(position, literals), literals, offset + length);
        }
        return new Token(dictionary.getToken(literals, Literals.IDENTIFIER), literals, offset + length);
    }

    private boolean isIdentifierChar(final char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || '_' == ch || '$' == ch || '#' == ch;
    }

    private boolean isAmbiguousIdentifier(final String literals) {
        return DefaultKeyword.ORDER.name().equalsIgnoreCase(literals) || DefaultKeyword.GROUP.name().equalsIgnoreCase(literals);
    }

    private TokenType processAmbiguousIdentifier(final int position, final String literals) {
        int i = 0;
        while (CharTypes.isWhitespace(charAt(position + i))) {
            i++;
        }
        if (DefaultKeyword.BY.name().equalsIgnoreCase(String.valueOf(new char[] {charAt(position + i), charAt(position + i + 1)}))) {
            return dictionary.getToken(literals);
        }
        return Literals.IDENTIFIER;
    }

    Token scanHexDecimal() {
        int length = 3;
        int position = offset + length - 1;
        if ('-' == charAt(position)) {
            position++;
            length++;
        }
        while (isHex(charAt(++position))) {
            length++;
        }
        return new Token(Literals.HEX, input.substring(offset, offset + length), offset + length);
    }

    private boolean isHex(final char ch) {
        return (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f') || (ch >= '0' && ch <= '9');
    }

    Token scanNumber() {
        int length = 0;
        int position = offset;
        if ('-' == charAt(position)) {
            position++;
            length++;
        }
        while (isDigital(charAt(position))) {
            length++;
            position++;
        }
        boolean isFloat = false;
        if ('.' == charAt(position)) {
            // TODO 待确认 数字后面加两个点表示什么
            if ('.' == charAt(position + 1)) {
                length++;
                return new Token(Literals.INT, input.substring(offset, offset + length), offset + length);
            }
            isFloat = true;
            position++;
            length++;
            while (isDigital(charAt(position))) {
                position++;
                length++;
            }
        }
        if ('e' == charAt(position) || 'E' == charAt(position)) {
            isFloat = true;
            position++;
            length++;
            if ('+' == charAt(position)) {
                position++;
                length++;
            }
            if ('-' == charAt(position)) {
                position++;
                length++;
            }
            while (isDigital(charAt(position))) {
                position++;
                length++;
            }
        }
        if ('f' == charAt(position) || 'F' == charAt(position)) {
            length++;
            return new Token(Literals.FLOAT, input.substring(offset, offset + length), offset + length);
        }
        if ('d' == charAt(position) || 'D' == charAt(position)) {
            length++;
            return new Token(Literals.FLOAT, input.substring(offset, offset + length), offset + length);
        }
        return new Token(isFloat ? Literals.FLOAT : Literals.INT, input.substring(offset, offset + length), offset + length);
    }

    private boolean isDigital(final char ch) {
        return ch >= '0' && ch <= '9';
    }

    Token scanChars() {
        int length = 1;
        int position = offset + length;
        while ('\'' != charAt(position) || hasEscapeChar(position)) {
            if (position >= input.length()) {
                throw new UnterminatedSignException('\'');
            }
            if (hasEscapeChar(position)) {
                length++;
                position++;
            }
            length++;
            position++;
        }
        length++;
        return new Token(Literals.CHARS, input.substring(offset + 1, offset + length - 1), offset + length);
    }

    private boolean hasEscapeChar(final int position) {
        return '\'' == charAt(position) && '\'' == charAt(position + 1);
    }

    Token scanSymbol(final int charLength) {
        int position = offset;
        int length = 0;
        char[] symbolChars = new char[charLength];
        for (int i = 0; i < charLength; i++) {
            symbolChars[i] = charAt(position++);
            length++;
        }
        String literals = String.valueOf(symbolChars);
        return new Token(Symbol.literalsOf(literals), literals, offset + length);
    }

    private char charAt(final int index) {
        return index >= input.length() ? (char) CharTypes.EOI : input.charAt(index);
    }
}