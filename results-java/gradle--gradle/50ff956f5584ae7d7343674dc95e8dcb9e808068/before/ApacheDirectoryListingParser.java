/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.internal.externalresource.transport.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApacheDirectoryListingParser {
    private static final Pattern PATTERN = Pattern.compile(
            "<a[^>]*href=\"([^\"]*)\"[^>]*>(?:<[^>]+>)*?([^<>]+?)(?:<[^>]+>)*?</a>",
            Pattern.CASE_INSENSITIVE);

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheDirectoryListingParser.class);
    private final URL inputUrl;

    public ApacheDirectoryListingParser(URL inputUrl) {
        this.inputUrl = inputUrl;
    }

    public List<URL> parse(String htmlText) throws IOException {
        List<URL> urlList = new ArrayList<URL>();
        Matcher matcher = PATTERN.matcher(htmlText);
        while (matcher.find()) {
            // get the href text and the displayed text
            String href = matcher.group(1);
            String text = matcher.group(2);
            text = text.trim();

            // handle complete URL listings
            if (href.startsWith("http:") || href.startsWith("https:")) {
                try {
                    href = new URL(href).getPath();
                    if (!href.startsWith(inputUrl.getPath())) {
                        // ignore URLs which aren't children of the base URL
                        continue;
                    }
                    href = href.substring(inputUrl.getPath().length());
                } catch (Exception ignore) {
                    // incorrect URL, ignore
                    continue;
                }
            }

            if (href.startsWith("../")) {
                // skip parent URLs
                continue;
            }

            // absolute href: convert to relative one
            if (href.startsWith("/")) {
                int slashIndex = href.substring(0, href.length() - 1).lastIndexOf('/');
                href = href.substring(slashIndex + 1);
            }

            // convert relative href to url
            if (href.startsWith("./")) {
                href = href.substring("./".length());
            }

            // exclude those where they do not match
            // href will never be truncated, text may be truncated by apache
            if (text.endsWith("..>")) {
                // text is probably truncated, we can only check if the href starts with text
                if (!href.startsWith(text.substring(0, text.length() - 3))) {
                    continue;
                }
            } else if (text.endsWith("..&gt;")) {
                // text is probably truncated, we can only check if the href starts with text
                if (!href.startsWith(text.substring(0, text.length() - 6))) {
                    continue;
                }
            } else {
                // text is not truncated, so it must match the url after stripping optional
                // trailing slashes
                String strippedHref = href.endsWith("/") ? href.substring(0, href.length() - 1) : href;
                String strippedText = text.endsWith("/") ? text.substring(0, text.length() - 1) : text;
                if (!strippedHref.equalsIgnoreCase(strippedText)) {
                    continue;
                }
            }
            URL child = new URL(inputUrl, href);
            urlList.add(child);
            LOGGER.debug("found URL=[" + child + "].");
        }
        return urlList;
    }
}