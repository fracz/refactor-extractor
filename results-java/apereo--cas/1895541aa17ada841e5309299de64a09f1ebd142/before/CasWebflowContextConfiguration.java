package org.jasig.cas.config;

import com.google.common.collect.ImmutableList;
import org.cryptacular.bean.CipherBean;
import org.jasig.cas.CipherExecutor;
import org.jasig.cas.web.flow.CasDefaultFlowUrlHandler;
import org.jasig.cas.web.flow.LogoutConversionService;
import org.jasig.cas.web.flow.SelectiveFlowHandlerAdapter;
import org.jasig.spring.webflow.plugin.EncryptedTranscoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.binding.convert.ConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.ResourceBundleViewResolver;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.expression.spel.WebFlowSpringELExpressionParser;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;

import javax.naming.OperationNotSupportedException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is {@link CasWebflowContextConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casWebflowContextConfiguration")
@Lazy(true)
public class CasWebflowContextConfiguration {

    private static final int LOGOUT_FLOW_HANDLER_ORDER = 3;

    private static final int VIEW_RESOLVER_ORDER = 10000;

    /**
     * The Resolver path prefix.
     */
    @Value("${cas.themeResolver.pathprefix:/WEB-INF/view/jsp}/default/ui/")
    private String resolverPathPrefix;

    @Autowired
    @Qualifier("logoutFlowExecutor")
    @Lazy(true)
    private FlowExecutor logoutFlowExecutor;

    @Autowired
    @Qualifier("loginFlowExecutor")
    @Lazy(true)
    private FlowExecutor loginFlowExecutor;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    @Lazy(true)
    private FlowDefinitionRegistry logoutFlowRegistry;

    @Autowired
    @Qualifier("loginFlowRegistry")
    @Lazy(true)
    private FlowDefinitionRegistry loginFlowRegistry;

    @Autowired
    @Qualifier("webflowCipherExecutor")
    private CipherExecutor<byte[], byte[]> webflowCipherExecutor;

    /**
     * The Key alias.
     */
    @Value("${cas.webflow.keyalias:aes128}")
    private String keyAlias;

    @Autowired
    @Qualifier("authenticationThrottle")
    @Lazy(true)
    private HandlerInterceptor authenticationThrottle;

    /**
     * Expression parser web flow spring el expression parser.
     *
     * @return the web flow spring el expression parser
     */
    @Bean(name = "expressionParser")
    public WebFlowSpringELExpressionParser expressionParser() {
        final WebFlowSpringELExpressionParser parser = new WebFlowSpringELExpressionParser(
                new SpelExpressionParser(),
                logoutConversionService());
        return parser;
    }

    /**
     * Logout conversion service conversion service.
     *
     * @return the conversion service
     */
    @Bean(name = "logoutConversionService")
    public ConversionService logoutConversionService() {
        return new LogoutConversionService();
    }

    /**
     * Internal view resolver internal resource view resolver.
     *
     * @return the internal resource view resolver
     */
    @Bean(name = "internalViewResolver")
    public InternalResourceViewResolver internalViewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix(this.resolverPathPrefix);
        resolver.setSuffix(".jsp");
        resolver.setOrder(VIEW_RESOLVER_ORDER);
        return resolver;
    }

    /**
     * View resolver resource bundle view resolver.
     *
     * @return the resource bundle view resolver
     */
    @Bean(name = "viewResolver")
    public ResourceBundleViewResolver viewResolver() {
        final ResourceBundleViewResolver resolver = new ResourceBundleViewResolver();
        resolver.setOrder(0);
        resolver.setBasename("cas_views");
        return resolver;
    }

    /**
     * View factory creator mvc view factory creator.
     *
     * @return the mvc view factory creator
     */
    @Bean(name = "viewFactoryCreator")
    public MvcViewFactoryCreator viewFactoryCreator() {
        final MvcViewFactoryCreator resolver = new MvcViewFactoryCreator();
        resolver.setViewResolvers(ImmutableList.of(viewResolver(), internalViewResolver()));
        return resolver;
    }


    /**
     * Login flow url handler cas default flow url handler.
     *
     * @return the cas default flow url handler
     */
    @Bean(name = "loginFlowUrlHandler")
    public CasDefaultFlowUrlHandler loginFlowUrlHandler() {
        return new CasDefaultFlowUrlHandler();
    }

    /**
     * Logout flow url handler cas default flow url handler.
     *
     * @return the cas default flow url handler
     */
    @Bean(name = "logoutFlowUrlHandler")
    public CasDefaultFlowUrlHandler logoutFlowUrlHandler() {
        final CasDefaultFlowUrlHandler handler = new CasDefaultFlowUrlHandler();
        handler.setFlowExecutionKeyParameter("RelayState");
        return handler;
    }

    /**
     * Logout handler adapter selective flow handler adapter.
     *
     * @return the selective flow handler adapter
     */
    @Bean(name = "logoutHandlerAdapter")
    public SelectiveFlowHandlerAdapter logoutHandlerAdapter() {
        final SelectiveFlowHandlerAdapter handler = new SelectiveFlowHandlerAdapter();
        handler.setSupportedFlowId("logout");
        handler.setFlowExecutor(this.logoutFlowExecutor);
        handler.setFlowUrlHandler(logoutFlowUrlHandler());
        return handler;
    }

    /**
     * Login flow cipher bean buffered block cipher bean.
     *
     * @return the buffered block cipher bean
     */
    @Bean(name = "loginFlowCipherBean")
    public CipherBean loginFlowCipherBean() {

        try {
            return new CipherBean() {
                @Override
                public byte[] encrypt(final byte[] bytes) {
                    return webflowCipherExecutor.encode(bytes);
                }

                @Override
                public void encrypt(final InputStream inputStream, final OutputStream outputStream) {
                    throw new RuntimeException(new OperationNotSupportedException("Encrypting input stream is not supported"));
                }

                @Override
                public byte[] decrypt(final byte[] bytes) {
                    return webflowCipherExecutor.decode(bytes);
                }

                @Override
                public void decrypt(final InputStream inputStream, final OutputStream outputStream) {
                    throw new RuntimeException(new OperationNotSupportedException("Decrypting input stream is not supported"));
                }
            };
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Login flow state transcoder encrypted transcoder.
     *
     * @return the encrypted transcoder
     */
    @Bean(name = "loginFlowStateTranscoder")
    public EncryptedTranscoder loginFlowStateTranscoder() {
        try {
            return new EncryptedTranscoder(loginFlowCipherBean());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Login handler adapter selective flow handler adapter.
     *
     * @return the selective flow handler adapter
     */
    @Bean(name = "loginHandlerAdapter")
    public SelectiveFlowHandlerAdapter loginHandlerAdapter() {
        final SelectiveFlowHandlerAdapter handler = new SelectiveFlowHandlerAdapter();
        handler.setSupportedFlowId("login");
        handler.setFlowExecutor(this.loginFlowExecutor);
        handler.setFlowUrlHandler(loginFlowUrlHandler());
        return handler;
    }

    /**
     * Locale change interceptor locale change interceptor.
     *
     * @return the locale change interceptor
     */
    @Bean(name = "localeChangeInterceptor")
    public LocaleChangeInterceptor localeChangeInterceptor() {
        return new LocaleChangeInterceptor();
    }

    /**
     * Logout flow handler mapping flow handler mapping.
     *
     * @return the flow handler mapping
     */
    @Bean(name = "logoutFlowHandlerMapping")
    public FlowHandlerMapping logoutFlowHandlerMapping() {
        final FlowHandlerMapping handler = new FlowHandlerMapping();
        handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER);
        handler.setFlowRegistry(logoutFlowRegistry);
        final Object[] interceptors = new Object[]{localeChangeInterceptor()};
        handler.setInterceptors(interceptors);
        return handler;
    }

    /**
     * Login flow handler mapping flow handler mapping.
     *
     * @return the flow handler mapping
     */
    @Bean(name = "loginFlowHandlerMapping")
    public FlowHandlerMapping loginFlowHandlerMapping() {
        final FlowHandlerMapping handler = new FlowHandlerMapping();
        handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER - 1);
        handler.setFlowRegistry(loginFlowRegistry);
        final Object[] interceptors = new Object[]{localeChangeInterceptor(), this.authenticationThrottle};
        handler.setInterceptors(interceptors);
        return handler;
    }

}
