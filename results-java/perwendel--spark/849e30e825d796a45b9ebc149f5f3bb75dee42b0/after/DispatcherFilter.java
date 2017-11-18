/***************************************************************
 *      _______  ______      ___       ______       __  ___    *
 *     /       ||   _  \    /   \     |   _  \     |  |/  /    *
 *    |   (----`|  |_)  |  /  ^  \    |  |_)  |    |  '  /     *
 *     \   \    |   ___/  /  /_\  \   |      /     |    <      *
 * .----)   |   |  |     /  _____  \  |  |\  \----.|  .  \     *
 * |_______/    | _|    /__/     \__\ | _| `._____||__|\__\    *
 *                                                             *
 **************************************************************/
package spark;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

/**
 * TODO: discover new TODOs.
 *
 * TODO: Add /uri/{param} possibility, DONE
 * TODO: Add *, splat possibility
 * TODO: Add validation of routes, invalid characters and stuff, also validate parameters, check static
 * TODO: Add possibility to access HttpServletContext in method impl.
 * TODO: Figure out a nice name, DONE - SPARK
 * TODO: Add possibility to set content type on return
 * TODO: Create maven archetype
 * TODO: Tweak log4j config, DONE
 * TODO: Add regexp URIs
 *
 * @author Per Wendel
 */
class DispatcherFilter implements Filter {

    private RouteMatcher routeMatcher;

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(DispatcherFilter.class);

    public void init(FilterConfig filterConfig) {
        routeMatcher = new RouteMatcher();

        Reflections reflections = new Reflections("", new MethodAnnotationsScanner());
        Set<Method> annotated = reflections.getMethodsAnnotatedWith(Route.class);

        LOG.info("Size: " + annotated.size());

        for (Method method : annotated) {
            Route s = method.getAnnotation(Route.class);
            String route = s.value().toLowerCase().trim();
            LOG.info("s: method: " + method + ", route: " + route);

            // Parse route string to get HttpMethod and route
            routeMatcher.parseValidateAddRoute(route, method);

            // TODO: Remember in a future paramContext ---> TO LOWER CASE !!!
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
        long t0 = System.currentTimeMillis();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String httpMethod = httpRequest.getMethod().toLowerCase();
        String uri = httpRequest.getRequestURI().toLowerCase();

        LOG.info("httpMethod:" + httpMethod + ", uri: " + uri);

        RouteMatch match = routeMatcher.findTargetForRoute(HttpMethod.valueOf(httpMethod), uri);

        Method target = null;

        WebContext webContext = null;
        if (match != null) {
            webContext = new WebContext(match);

            // TODO: Do something with the web context

            target = match.getTarget();
        }

        System.out.println("target: " + target);

        if (target != null) {
            try {
                Object result;
                if (Modifier.isStatic(target.getModifiers())) {
                    target.getParameterTypes();
                    if (SparkUtils.hasWebContext(target)) {
                        result = target.invoke(target.getDeclaringClass(), webContext);
                    } else {
                        result = target.invoke(target.getDeclaringClass());
                    }

                } else {
                    LOG.warn("Method: '" + target + "' should be static. " + "Spark" + " is nice and will try to invoke it anyways...");
                    httpResponse.sendError(500, "Target method not static");
                    return;
                }
                httpResponse.getOutputStream().write(result.toString().getBytes("utf-8"));
                long t1 = System.currentTimeMillis() - t0;
                LOG.info("Time for request: " + t1);
            } catch (Exception e) {
                LOG.error(e);
            }
        } else {
            httpResponse.sendError(404);
        }
    }

    public void destroy() {
        // TODO Auto-generated method stub
    }
}