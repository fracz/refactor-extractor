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

import com.google.inject.Injector;
import com.google.inject.Key;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * An internal representation of a servlet definition mapped to a particular URI pattern. Also
 * performs the request dispatch to that servlet. How nice and OO =)
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class ServletDefinition {
  private final String pattern;
  private final Key<? extends HttpServlet> servletKey;
  private final UriPatternMatcher patternMatcher;
  private final Map<String, String> initParams;

  //our servlet is always presumed to be a singleton
  private final AtomicReference<HttpServlet> httpServlet = new AtomicReference<HttpServlet>();

  public ServletDefinition(String pattern, Key<? extends HttpServlet> servletKey,
      UriPatternMatcher patternMatcher, Map<String, String> initParams) {
    this.pattern = pattern;
    this.servletKey = servletKey;
    this.patternMatcher = patternMatcher;
    this.initParams = Collections.unmodifiableMap(new HashMap<String, String>(initParams));
  }

  boolean shouldServe(String uri) {
    return patternMatcher.matches(uri, pattern);
  }

  public void init(final ServletContext servletContext, Injector injector) throws ServletException {
    //TODO this absolutely must be a singleton, and so is only initialized once
    HttpServlet httpServlet = injector.getInstance(servletKey);
    this.httpServlet.set(httpServlet);

    //initialize our servlet with the configured context params and servlet context
    //noinspection OverlyComplexAnonymousInnerClass,AnonymousInnerClassWithTooManyMethods
    httpServlet.init(new ServletConfig() {
      public String getServletName() {
        return servletKey.toString();
      }

      public ServletContext getServletContext() {
        return servletContext;
      }

      public String getInitParameter(String s) {
        return initParams.get(s);
      }

      public Enumeration getInitParameterNames() {
        //noinspection InnerClassTooDeeplyNested,AnonymousInnerClassWithTooManyMethods
        return new Enumeration() {
          private final Iterator<String> paramNames = initParams.keySet().iterator();

          public boolean hasMoreElements() {
            return paramNames.hasNext();
          }

          public Object nextElement() {
            return paramNames.next();
          }
        };
      }
    });
  }

  public void destroy() {
    httpServlet.get().destroy();
  }

  /**
   * Wrapper around the service chain to ensure a servlet is servicing what it must and provides it
   * with a wrapped request.
   *
   * @return Returns true if this servlet triggered for the given request. Or false if
   *          guice-servlet should continue dispatching down the servlet pipeline.
   *
   * @throws IOException If thrown by underlying servlet
   * @throws ServletException If thrown by underlying servlet
   */
  public boolean service(ServletRequest servletRequest,
      ServletResponse servletResponse) throws IOException, ServletException {

    final boolean serve = shouldServe(((HttpServletRequest) servletRequest).getServletPath());

    //invocations of the chain end at the first matched servlet
    if (serve) {
      doService(servletRequest, servletResponse);
    }

    //return false if no servlet matched (so we can proceed down to the web.xml servlets)
    return serve;
  }

  /**
   * Utility that delegates to the actual service method of the servlet wrapped with a contextual
   * request (i.e. with correctly computed path info).
   *
   * We need to suppress deprecation coz we use HttpServletRequestWrapper, which implements
   * deprecated API for backwards compatibility.
   */
  @SuppressWarnings({ "JavaDoc", "deprecation" }) void doService(
      final ServletRequest servletRequest, ServletResponse servletResponse)
      throws ServletException, IOException {

    //noinspection OverlyComplexAnonymousInnerClass
    HttpServletRequest request = new HttpServletRequestWrapper(
        (HttpServletRequest) servletRequest) {
      private String path;
      private boolean pathComputed = false;
      //must use a boolean on the memo field, because null is a legal value (TODO no, it's not)

      private boolean pathInfoComputed = false;
      private String pathInfo;

      @Override
      public String getPathInfo() {
        if (!pathInfoComputed) {
          final int servletPathLength = getServletPath().length();
          pathInfo = getRequestURI().substring(getContextPath().length()).replaceAll("[/]{2,}", "/")
              .substring(servletPathLength);

          //corner case: when servlet path and request path match exactly (without trailing '/'),
          //pathinfo is null
          if ("".equals(pathInfo) && servletPathLength != 0) {
            pathInfo = null;
          }

          pathInfoComputed = true;
        }

        return pathInfo;
      }

      @Override
      public String getServletPath() {
        return computePath();
      }

      @Override
      public String getPathTranslated() {
        final String info = getPathInfo();

        return (null == info) ? null : getRealPath(info);
      }

      //memoizer pattern
      private String computePath() {
        if (!pathComputed) {
          path = patternMatcher.extractPath(pattern);
          pathComputed = true;

          if (null == path) {
            path = super.getServletPath();
          }
        }

        return path;
      }
    };

    httpServlet.get().service(request, servletResponse);
  }

}