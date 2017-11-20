package org.apereo.cas.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * This is {@link JpaEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jpaEventsConfiguration")
public class JpaEventsConfiguration {

    /**
     * The Show sql.
     */
    @Value("${database.show.sql:true}")
    private boolean showSql;

    /**
     * The Generate ddl.
     */
    @Value("${database.gen.ddl:true}")
    private boolean generateDdl;

    /**
     * The Hibernate dialect.
     */
    @Value("${events.jpa.database.dialect:org.hibernate.dialect.HSQLDialect}")
    private String hibernateDialect;

    /**
     * The Hibernate hbm 2 ddl auto.
     */
    @Value("${events.jpa.database.ddl.auto:create-drop}")
    private String hibernateHbm2DdlAuto;

    /**
     * The Hibernate batch size.
     */
    @Value("${events.jpa.database.batchSize:1}")
    private String hibernateBatchSize;

    /**
     * The Driver class.
     */
    @Value("${events.jpa.database.driverClass:org.hsqldb.jdbcDriver}")
    private String driverClass;

    /**
     * The Jdbc url.
     */
    @Value("${events.jpa.database.url:jdbc:hsqldb:mem:cas-events-registry}")
    private String jdbcUrl;

    /**
     * The User.
     */
    @Value("${events.jpa.database.user:sa}")
    private String user;

    /**
     * The Password.
     */
    @Value("${events.jpa.database.password:}")
    private String password;

    /**
     * The Max pool size.
     */
    @Value("${events.jpa.database.pool.maxSize:18}")
    private int maxPoolSize;

    /**
     * The Max idle time excess connections.
     */
    @Value("${events.jpa.database.pool.maxIdleTime:1000}")
    private int maxIdleTimeExcessConnections;

    /**
     * The Checkout timeout.
     */
    @Value("${events.jpa.database.pool.maxWait:2000}")
    private int checkoutTimeout;



    /**
     * Jpa event vendor adapter hibernate jpa vendor adapter.
     *
     * @return the hibernate jpa vendor adapter
     */
    @RefreshScope
    @Bean(name = "jpaEventVendorAdapter")
    public HibernateJpaVendorAdapter jpaEventVendorAdapter() {
        final HibernateJpaVendorAdapter jpaEventVendorAdapter = new HibernateJpaVendorAdapter();
        jpaEventVendorAdapter.setGenerateDdl(this.generateDdl);
        jpaEventVendorAdapter.setShowSql(this.showSql);
        return jpaEventVendorAdapter;
    }


    /**
     * Data source event combo pooled data source.
     *
     * @return the combo pooled data source
     */
    @RefreshScope
    @Bean(name = "dataSourceEvent")
    public DataSource dataSourceEvent() {
        try {
            final HikariDataSource bean = new HikariDataSource();
            bean.setDriverClassName(this.driverClass);
            bean.setJdbcUrl(this.jdbcUrl);
            bean.setUsername(this.user);
            bean.setPassword(this.password);

            bean.setMaximumPoolSize(this.maxPoolSize);
            bean.setMinimumIdle(this.maxIdleTimeExcessConnections);

            bean.setLoginTimeout(this.checkoutTimeout);
            bean.setValidationTimeout(this.checkoutTimeout);

            return bean;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Jpa event packages to scan string [ ].
     *
     * @return the string [ ]
     */
    @Bean(name = "jpaEventPackagesToScan")
    public String[] jpaEventPackagesToScan() {
        return new String[]{"org.apereo.cas.support.events.dao"};
    }

    /**
     * Events entity manager factory local container entity manager factory bean.
     *
     * @return the local container entity manager factory bean
     */
    @RefreshScope
    @Bean(name = "eventsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean eventsEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

        bean.setJpaVendorAdapter(jpaEventVendorAdapter());
        bean.setPersistenceUnitName("jpaEventRegistryContext");
        bean.setPackagesToScan(jpaEventPackagesToScan());
        bean.setDataSource(dataSourceEvent());

        final Properties properties = new Properties();
        properties.put("hibernate.dialect", this.hibernateDialect);
        properties.put("hibernate.hbm2ddl.auto", this.hibernateHbm2DdlAuto);
        properties.put("hibernate.jdbc.batch_size", this.hibernateBatchSize);
        properties.put("hibernate.enable_lazy_load_no_trans", Boolean.TRUE);
        bean.setJpaProperties(properties);
        return bean;
    }


    /**
     * Transaction manager events jpa transaction manager.
     *
     * @param emf the emf
     * @return the jpa transaction manager
     */
    @Bean(name = "transactionManagerEvents")
    public JpaTransactionManager transactionManagerEvents(@Qualifier("eventsEntityManagerFactory")
                                                          final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }


}