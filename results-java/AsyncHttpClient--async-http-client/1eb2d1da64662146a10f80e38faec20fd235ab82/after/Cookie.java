/*
 * Copyright (c) 2014 AsyncHttpClient Project. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.asynchttpclient.cookie;

public class Cookie {

    public static Cookie newValidCookie(String domain, String name, String value, String path, int maxAge, boolean secure) {
        return newValidCookie(domain, name, value, value, path, maxAge, secure, false);
    }

    public static Cookie newValidCookie(String domain, String name, String value, String rawValue, String path, int maxAge, boolean secure, boolean httpOnly) {

        if (name == null) {
            throw new NullPointerException("name");
        }
        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c > 127) {
                throw new IllegalArgumentException("name contains non-ascii character: " + name);
            }

            // Check prohibited characters.
            switch (c) {
            case '\t':
            case '\n':
            case 0x0b:
            case '\f':
            case '\r':
            case ' ':
            case ',':
            case ';':
            case '=':
                throw new IllegalArgumentException("name contains one of the following prohibited characters: " + "=,; \\t\\r\\n\\v\\f: " + name);
            }
        }

        if (name.charAt(0) == '$') {
            throw new IllegalArgumentException("name starting with '$' not allowed: " + name);
        }

        if (value == null) {
            throw new NullPointerException("value");
        }

        domain = validateValue("domain", domain);
        path = validateValue("path", path);

        return new Cookie(domain, name, value, rawValue, path, maxAge, secure, httpOnly);
    }

    private static String validateValue(String name, String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
            case '\r':
            case '\n':
            case '\f':
            case 0x0b:
            case ';':
                throw new IllegalArgumentException(name + " contains one of the following prohibited characters: " + ";\\r\\n\\f\\v (" + value + ')');
            }
        }
        return value;
    }

    private final String domain;
    private final String name;
    private final String value;
    private final String rawValue;
    private final String path;
    private final int maxAge;
    private final boolean secure;
    private final boolean httpOnly;

    public Cookie(String domain, String name, String value, String rawValue, String path, int maxAge, boolean secure, boolean httpOnly) {
        this.domain = domain;
        this.name = name;
        this.value = value;
        this.rawValue = rawValue;
        this.path = path;
        this.maxAge = maxAge;
        this.secure = secure;
        this.httpOnly = httpOnly;
    }

    public String getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getRawValue() {
        return rawValue;
    }

    public String getPath() {
        return path;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(name);
        buf.append("=");
        buf.append(rawValue);
        if (getDomain() != null) {
            buf.append("; domain=");
            buf.append(domain);
        }
        if (getPath() != null) {
            buf.append("; path=");
            buf.append(path);
        }
        if (getMaxAge() >= 0) {
            buf.append("; maxAge=");
            buf.append(maxAge);
            buf.append("s");
        }
        if (secure) {
            buf.append("; secure");
        }
        if (httpOnly) {
            buf.append("; HTTPOnly");
        }
        return buf.toString();
    }
}