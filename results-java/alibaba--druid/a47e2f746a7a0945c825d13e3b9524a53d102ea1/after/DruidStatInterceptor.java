package com.alibaba.druid.support.spring.stat;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.druid.filter.stat.StatFilterContext;
import com.alibaba.druid.filter.stat.StatFilterContextListenerAdapter;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;

public class DruidStatInterceptor implements MethodInterceptor, InitializingBean, DisposableBean {

    private final static Log            LOG                 = LogFactory.getLog(DruidStatInterceptor.class);

    private static SpringStat           springStat          = new SpringStat();

    private SpringMethodContextListener statContextlistener = new SpringMethodContextListener();

    public DruidStatInterceptor(){

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SpringStatManager.getInstance().addSpringStat(springStat);

        StatFilterContext.getInstance().addContextListener(statContextlistener);
    }

    @Override
    public void destroy() throws Exception {
        StatFilterContext.getInstance().removeContextListener(statContextlistener);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        SpringMethodStat lastMethodStat = SpringMethodStat.current();

        SpringMethodInfo methodInfo = getMethodInfo(invocation);

        SpringMethodStat methodStat = springStat.getMethodStat(methodInfo, true);

        if (methodStat != null) {
            methodStat.beforeInvoke();
        }

        long startNanos = System.nanoTime();

        Throwable error = null;
        try {
            Object returnObject = invocation.proceed();

            return returnObject;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long endNanos = System.nanoTime();

            long nanos = endNanos - startNanos;

            if (methodStat != null) {
                methodStat.afterInvoke(error, nanos);
            }

            SpringMethodStat.setCurrent(lastMethodStat);
        }
    }

    public SpringMethodInfo getMethodInfo(MethodInvocation invocation) {
        Object thisObject = invocation.getThis();
        Method method = invocation.getMethod();

        if (thisObject == null) {
            return new SpringMethodInfo(method.getDeclaringClass(), method);
        }

        if (method.getDeclaringClass() == thisObject.getClass()) {
            return new SpringMethodInfo(method.getDeclaringClass(), method);
        }

        {
            Class<?> clazz = thisObject.getClass();
            boolean isCglibProxy = false;
            boolean isJavassistProxy = false;
            for (Class<?> item : clazz.getInterfaces()) {
                if (item.getName().equals("net.sf.cglib.proxy.Factory")) {
                    isCglibProxy = true;
                    break;
                } else if (item.getName().equals("javassist.util.proxy.ProxyObject")) {
                    isJavassistProxy = true;
                    break;
                }
            }

            if (isCglibProxy || isJavassistProxy) {
                Class<?> superClazz = clazz.getSuperclass();

                return new SpringMethodInfo(superClazz, method);
            }
        }

        Class<?> clazz = null;
        try {
            // 最多支持10层代理
            for (int i = 0; i < 10; ++i) {
                if (thisObject instanceof org.springframework.aop.framework.Advised) {
                    TargetSource targetSource = ((org.springframework.aop.framework.Advised) thisObject).getTargetSource();

                    if (targetSource == null) {
                        break;
                    }

                    Object target = targetSource.getTarget();
                    if (target != null) {
                        thisObject = target;
                    } else {
                        clazz = targetSource.getTargetClass();
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Exception ex) {
            LOG.error("getMethodInfo error", ex);
        }

        if (clazz == null) {
            return new SpringMethodInfo(method.getDeclaringClass(), method);
        }

        return new SpringMethodInfo(clazz, method);
    }

    class SpringMethodContextListener extends StatFilterContextListenerAdapter {

        @Override
        public void addUpdateCount(int updateCount) {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.addJdbcUpdateCount(updateCount);
            }
        }

        @Override
        public void addFetchRowCount(int fetchRowCount) {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.addJdbcFetchRowCount(fetchRowCount);
            }
        }

        @Override
        public void executeBefore(String sql, boolean inTransaction) {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.incrementJdbcExecuteCount();
            }
        }

        @Override
        public void executeAfter(String sql, long nanos, Throwable error) {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.addJdbcExecuteTimeNano(nanos);
                if (error != null) {
                    springMethodStat.incrementJdbcExecuteErrorCount();
                }
            }
        }

        @Override
        public void commit() {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.incrementJdbcCommitCount();
            }
        }

        @Override
        public void rollback() {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.incrementJdbcRollbackCount();
            }
        }

        @Override
        public void pool_connect() {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.incrementJdbcPoolConnectionOpenCount();
            }
        }

        @Override
        public void pool_close(long nanos) {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.incrementJdbcPoolConnectionCloseCount();
            }
        }

        @Override
        public void resultSet_open() {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.incrementJdbcResultSetOpenCount();
            }
        }

        @Override
        public void resultSet_close(long nanos) {
            SpringMethodStat springMethodStat = SpringMethodStat.current();
            if (springMethodStat != null) {
                springMethodStat.incrementJdbcResultSetCloseCount();
            }
        }
    }
}