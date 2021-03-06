/**
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.servlet;

/**
 * <p>
 * A general interface for matching a URI against a URI pattern. Guice-servlet provides
 * regex and servlet-style pattern matching out of the box.
 * </p>
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 * @see com.google.inject.servlet.Servlets#configure() Binding Servlets.
 */
interface UriPatternMatcher {
    /**
     * @param uri A "contextual" (i.e. relative) Request URI, *not* a complete one.
     * @param pattern A String containing some pattern that this service can match against.
     * @return Returns true if the uri matches the pattern.
     */
    boolean matches(String uri, String pattern);

    /**
     * @param pattern A String containing some pattern that this service can match against.
     * @return Returns a canonical servlet path from this pattern. For instance, if the
     * pattern is {@code /home/*} then the path extracted will be {@code /home}. Each pattern
     * matcher implementation must decide and publish what a canonical path represents.
     *
     * NOTE(dhanji) This method returns null for the regex pattern matcher.
     *
     */
    String extractPath(String pattern);
}