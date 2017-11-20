package org.jasig.cas.config;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasMetricsConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casMetricsConfiguration")
@EnableMetrics
public class CasMetricsConfiguration extends MetricsConfigurerAdapter {

    @Value("${metrics.refresh.internal:30}")
    private long perfStatsPeriod;

    @Value("${metrics.logger.name:perfStatsLogger}")
    private String perfStatsLoggerName;

    /**
     * Metric registry metric registry.
     *
     * @return the metric registry
     */
    @Bean(name = "metrics")
    public MetricRegistry metricRegistry() {
        final MetricRegistry metrics = new MetricRegistry();
        metrics.register("jvm.gc", new GarbageCollectorMetricSet());
        metrics.register("jvm.memory", new MemoryUsageGaugeSet());
        metrics.register("thread-states", new ThreadStatesGaugeSet());
        metrics.register("jvm.fd.usage", new FileDescriptorRatioGauge());
        return metrics;
    }
    /**
     * Metrics servlet servlet registration bean.
     *
     * @return the servlet registration bean
     */
    @Bean(name="metricsServlet")
    public ServletRegistrationBean metricsServlet() {
        final ServletRegistrationBean bean = new ServletRegistrationBean();
        bean.setEnabled(true);
        bean.setName("metricsServlet");
        bean.setServlet(new MetricsServlet());
        bean.setUrlMappings(Collections.singleton("/status/metrics"));
        bean.setLoadOnStartup(1);
        return bean;
    }

    /**
     * Health check metrics health check registry.
     *
     * @return the health check registry
     */
    @Bean(name = "healthCheckMetrics")
    public HealthCheckRegistry healthCheckMetrics() {
        return new HealthCheckRegistry();
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry();
    }

    @Override
    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckMetrics();
    }

    @Override
    public void configureReporters(final MetricRegistry metricRegistry) {
        final Logger perfStatsLogger = LoggerFactory.getLogger(this.perfStatsLoggerName);
        registerReporter(Slf4jReporter
                .forRegistry(metricRegistry)
                .outputTo(perfStatsLogger)
                .convertRatesTo(TimeUnit.MILLISECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build())
                .start(this.perfStatsPeriod, TimeUnit.SECONDS);

        registerReporter(JmxReporter
                .forRegistry(metricRegistry)
                .build());

    }
}