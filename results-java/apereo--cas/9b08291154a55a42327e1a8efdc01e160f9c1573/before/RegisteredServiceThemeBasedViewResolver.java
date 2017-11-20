package org.jasig.cas.services.web;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategyUtils;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.template.TemplateLocation;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Comment;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Macro;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.AbstractThymeleafView;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.ITemplateModeHandler;
import org.thymeleaf.templatemode.TemplateModeHandler;
import org.thymeleaf.templateparser.xmlsax.XhtmlAndHtml5NonValidatingSAXTemplateParser;
import org.thymeleaf.templatewriter.AbstractGeneralTemplateWriter;
import org.thymeleaf.templatewriter.ITemplateWriter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * {@link RegisteredServiceThemeBasedViewResolver} is an alternate Spring View Resolver that utilizes a service's
 * associated theme to selectively choose which set of UI views will be used to generate
 * the standard views.
 *
 * @author John Gasper
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RefreshScope
@Component("registeredServiceViewResolver")
public class RegisteredServiceThemeBasedViewResolver extends ThymeleafViewResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceThemeBasedViewResolver.class);

    @Autowired
    private ThymeleafProperties properties;

    @Autowired
    @Qualifier("thymeleafViewResolver")
    private ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Resource(name = "argumentExtractors")
    private List argumentExtractors;

    /**
     * Instantiates a new Registered service theme based view resolver.
     */
    public RegisteredServiceThemeBasedViewResolver() {
    }

    /**
     * The {@link RegisteredServiceThemeBasedViewResolver} constructor.
     *
     * @param servicesManager the serviceManager implementation
     */
    public RegisteredServiceThemeBasedViewResolver(final ServicesManager servicesManager) {
        super();
        this.servicesManager = servicesManager;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        setApplicationContext(this.thymeleafViewResolver.getApplicationContext());
        setCache(this.properties.isCache());
        if (!isCache()) {
            setCacheLimit(0);
        }
        setCacheUnresolved(this.thymeleafViewResolver.isCacheUnresolved());
        setCharacterEncoding(this.thymeleafViewResolver.getCharacterEncoding());
        setContentType(this.thymeleafViewResolver.getContentType());
        setExcludedViewNames(this.thymeleafViewResolver.getExcludedViewNames());
        setOrder(this.thymeleafViewResolver.getOrder());
        setRedirectContextRelative(this.thymeleafViewResolver.isRedirectContextRelative());
        setRedirectHttp10Compatible(this.thymeleafViewResolver.isRedirectHttp10Compatible());
        setStaticVariables(this.thymeleafViewResolver.getStaticVariables());

        final SpringTemplateEngine engine = this.thymeleafViewResolver.getTemplateEngine();
        if (!engine.isInitialized()) {
            final ITemplateWriter writer = new AbstractGeneralTemplateWriter() {
                @Override
                protected boolean shouldWriteXmlDeclaration() {
                    return false;
                }

                @Override
                protected boolean useXhtmlTagMinimizationRules() {
                    return true;
                }

                @Override
                protected void writeText(final Arguments arguments, final Writer writer, final Text text) throws IOException {
                    final String contentString = text.getContent();
                    if (!contentString.isEmpty() && contentString.trim().isEmpty()) {
                        return;
                    }
                    super.writeText(arguments, writer, text);
                }

                @Override
                public void writeNode(final Arguments arguments, final Writer writer, final Node node)
                        throws IOException {
                    super.writeNode(arguments, writer, node);
                    if ((node instanceof Element) || (node instanceof Comment) || (node instanceof Macro)) {
                        writer.write("\n");
                    }
                }
            };

            final ITemplateModeHandler handler = new TemplateModeHandler("HTML5",
                    new XhtmlAndHtml5NonValidatingSAXTemplateParser(2), writer);
            engine.setTemplateModeHandlers(Collections.singleton(handler));
        }
        setTemplateEngine(engine);
        setViewNames(this.thymeleafViewResolver.getViewNames());

    }

    @Override
    protected View loadView(final String viewName, final Locale locale) throws Exception {
        final View view = super.loadView(viewName, locale);

        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final WebApplicationService service;

        if (requestContext != null) {
            service = WebUtils.getService(this.argumentExtractors, requestContext);
        } else {
            final HttpServletRequest request = WebUtils.getHttpServletRequestFromRequestAttributes();
            service = WebUtils.getService(this.argumentExtractors, request);
        }

        if (service == null) {
            return view;
        }

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService != null) {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            if (StringUtils.hasText(registeredService.getTheme())  && view instanceof AbstractThymeleafView) {
                LOGGER.debug("Attempting to locate views for service [{}] with theme [{}]",
                        registeredService.getServiceId(), registeredService.getTheme());

                final AbstractThymeleafView thymeleafView = (AbstractThymeleafView) view;
                final String viewUrl = registeredService.getTheme() + '/' + thymeleafView.getTemplateName();

                final String viewLocationUrl = this.properties.getPrefix().concat(viewUrl).concat(this.properties.getSuffix());
                LOGGER.debug("Attempting to locate view at {}", viewLocationUrl);
                final TemplateLocation location = new TemplateLocation(viewLocationUrl);
                if (location.exists(getApplicationContext())) {
                    LOGGER.debug("Found view {}", viewUrl);
                    thymeleafView.setTemplateName(viewUrl);
                } else {
                    LOGGER.debug("View {} does not exist. Fallling back to default view at {}",
                            viewLocationUrl, thymeleafView.getTemplateName());
                }

            }
        }
        return view;
    }
}