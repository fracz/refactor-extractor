package com.alibaba.druid.stat;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.JMException;

import com.alibaba.druid.VERSION;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.ReflectionUtils;

/**
 * 监控相关的对外数据暴露
 *
 * <pre>
 * 1. 为了支持jndi数据源本类内部调用druid相关对象均需要反射调用,返回值也应该是Object,List<Object>,Map<String,Object>等无关于druid的类型
 * 2. 对外暴露的public方法都应该先调用init()，应该有更好的方式，暂时没想到
 * </pre>
 *
 * @author sandzhang<sandzhangtoo@gmail.com>
 */
public class DruidStatManagerFacade {

    private final static DruidStatManagerFacade instance = new DruidStatManagerFacade();

    private boolean                             inited   = false;

    protected final ReentrantLock               lock     = new ReentrantLock(true);

    private Object                              druidDataSourceStatManager;
    private Object                              jdbcStatManager;

    // TODO shoud be Object
    private Set<DruidDataSource>                datasourceInstances;

    private DruidStatManagerFacade() {
    }

    public static DruidStatManagerFacade getInstance() {
        return instance;
    }

    private void init() {
        if (inited) {
            return;
        }

        lock.lock();
        try {
            if (inited) {
                return;
            }

            initByClassLoader();

            inited = true;
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void initByClassLoader() {
        Class<?> druidDataSourceStatManagerClass = ReflectionUtils.getClassFromWebContainerOrCurrentClassLoader("com.alibaba.druid.stat.DruidDataSourceStatManager");

        druidDataSourceStatManager = (DruidDataSourceStatManager) ReflectionUtils.callStaticMethod(druidDataSourceStatManagerClass,
                                                                                                   "getInstance");
        datasourceInstances = (Set<DruidDataSource>) ReflectionUtils.callStaticMethod(druidDataSourceStatManagerClass,
                                                                                      "getDruidDataSourceInstances");

        Class<?> jdbcStatManagerClass = ReflectionUtils.getClassFromWebContainerOrCurrentClassLoader("com.alibaba.druid.stat.JdbcStatManager");
        jdbcStatManager = (JdbcStatManager) ReflectionUtils.callStaticMethod(jdbcStatManagerClass, "getInstance");

    }

    private Set<DruidDataSource> getDruidDataSourceInstances() {
        init();
        return datasourceInstances;
    }

    public void resetDataSourceStat() {
        init();
        ReflectionUtils.callObjectMethod(druidDataSourceStatManager, "reset");
    }

    public void resetSqlStat() {
        init();
        ReflectionUtils.callObjectMethod(jdbcStatManager, "reset");
    }

    public void resetAll() {
        init();
        resetSqlStat();
        resetDataSourceStat();
    }

    private JdbcSqlStat getSqlStatById(Integer id) {

        for (DruidDataSource ds : getDruidDataSourceInstances()) {
            JdbcSqlStat sqlStat = ds.getDataSourceStat().getSqlStat(id);
            if (sqlStat != null) return sqlStat;
        }
        return null;
    }

    public Map<String, Object> getDataSourceStatData(Integer id) {
        init();
        if (id == null) {
            return null;
        }
        DruidDataSource datasource = getDruidDataSourceById(id);
        return datasource == null ? null : dataSourceToMapData(datasource);
    }

    private DruidDataSource getDruidDataSourceById(Integer identity) {

        if (identity == null) {
            return null;
        }

        for (DruidDataSource datasource : getDruidDataSourceInstances()) {
            if (System.identityHashCode(datasource) == identity) {
                return datasource;
            }
        }
        return null;
    }

    public List<Map<String, Object>> getSqlStatDataList() {
        init();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (DruidDataSource datasource : getDruidDataSourceInstances()) {
            for (JdbcSqlStat sqlStat : datasource.getDataSourceStat().getSqlStatMap().values()) {
                if (sqlStat.getExecuteCount() == 0 && sqlStat.getRunningCount() == 0) {
                    continue;
                }

                result.add(getSqlStatData(sqlStat));
            }
        }
        return result;
    }

    public Map<String, Object> getSqlStatData(Integer id) {
        init();
        if (id == null) {
            return null;
        }
        JdbcSqlStat sqlStat = getSqlStatById(id);
        return sqlStat == null ? null : getSqlStatData(sqlStat);
    }

    private Map<String, Object> getSqlStatData(JdbcSqlStat sqlStat) {
        try {
            return sqlStat.getData();
        } catch (JMException e) {
        }
        return null;
    }

    public List<Object> getDataSourceStatList() {
        init();
        List<Object> datasourceList = new ArrayList<Object>();
        for (DruidDataSource dataSource : getDruidDataSourceInstances()) {
            datasourceList.add(dataSourceToMapData(dataSource));
        }
        return datasourceList;
    }

    public Map<String, Object> returnJSONBasicStat() {
        init();
        Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
        dataMap.put("Version", VERSION.getVersionNumber());
        dataMap.put("Drivers", getDriversData());
        return dataMap;
    }

    private List<String> getDriversData() {
        List<String> drivers = new ArrayList<String>();
        for (Enumeration<Driver> e = DriverManager.getDrivers(); e.hasMoreElements();) {
            Driver driver = e.nextElement();
            drivers.add(driver.getClass().getName());
        }
        return drivers;
    }

    public List<Map<String, Object>> getPoolingConnectionInfoByDataSourceId(Integer id) {
        init();
        DruidDataSource datasource = getDruidDataSourceById(id);
        if (datasource == null) return null;
        return datasource.getPoolingConnectionInfo();
    }

    public List<String> getActiveConnectionStackTraceByDataSourceId(Integer id) {
        init();
        DruidDataSource datasource = getDruidDataSourceById(id);
        if (datasource == null || !datasource.isRemoveAbandoned()) return null;

        return datasource.getActiveConnectionStackTrace();
    }

    private Map<String, Object> dataSourceToMapData(DruidDataSource dataSource) {

        Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
        dataMap.put("Identity", System.identityHashCode(dataSource));
        dataMap.put("Name", dataSource.getName());
        dataMap.put("DbType", dataSource.getDbType());
        dataMap.put("DriverClassName", dataSource.getDriverClassName());

        dataMap.put("URL", dataSource.getUrl());
        dataMap.put("UserName", dataSource.getUsername());
        dataMap.put("FilterClassNames", dataSource.getFilterClassNames());

        dataMap.put("WaitThreadCount", dataSource.getWaitThreadCount());
        dataMap.put("NotEmptyWaitCount", dataSource.getNotEmptyWaitCount());
        dataMap.put("NotEmptyWaitMillis", dataSource.getNotEmptyWaitMillis());

        dataMap.put("PoolingCount", dataSource.getPoolingCount());
        dataMap.put("PoolingPeak", dataSource.getPoolingPeak());
        dataMap.put("PoolingPeakTime",
                    dataSource.getPoolingPeakTime() == null ? null : dataSource.getPoolingPeakTime().toString());

        dataMap.put("ActiveCount", dataSource.getActiveCount());
        dataMap.put("ActivePeak", dataSource.getActivePeak());
        dataMap.put("ActivePeakTime",
                    dataSource.getActivePeakTime() == null ? null : dataSource.getActivePeakTime().toString());

        dataMap.put("InitialSize", dataSource.getInitialSize());
        dataMap.put("MinIdle", dataSource.getMinIdle());
        dataMap.put("MaxActive", dataSource.getMaxActive());

        dataMap.put("QueryTimeout", dataSource.getQueryTimeout());
        dataMap.put("TransactionQueryTimeout", dataSource.getTransactionQueryTimeout());
        dataMap.put("LoginTimeout", dataSource.getLoginTimeout());
        dataMap.put("ValidConnectionCheckerClassName", dataSource.getValidConnectionCheckerClassName());
        dataMap.put("ExceptionSorterClassName", dataSource.getExceptionSorterClassName());

        dataMap.put("TestOnBorrow", dataSource.isTestOnBorrow());
        dataMap.put("TestOnReturn", dataSource.isTestOnReturn());
        dataMap.put("TestWhileIdle", dataSource.isTestWhileIdle());

        dataMap.put("DefaultAutoCommit", dataSource.isDefaultAutoCommit());
        dataMap.put("DefaultReadOnly", dataSource.isDefaultAutoCommit());
        dataMap.put("DefaultTransactionIsolation", dataSource.getDefaultTransactionIsolation());

        dataMap.put("LogicConnectCount", dataSource.getConnectCount());
        dataMap.put("LogicCloseCount", dataSource.getCloseCount());
        dataMap.put("LogicConnectErrorCount", dataSource.getConnectErrorCount());

        dataMap.put("PhysicalConnectCount", dataSource.getCreateCount());
        dataMap.put("PhysicalCloseCount", dataSource.getDestroyCount());
        dataMap.put("PhysicalConnectErrorCount", dataSource.getCreateErrorCount());

        dataMap.put("ExecuteCount", dataSource.getExecuteCount());
        dataMap.put("ErrorCount", dataSource.getErrorCount());
        dataMap.put("CommitCount", dataSource.getCommitCount());
        dataMap.put("RollbackCount", dataSource.getRollbackCount());

        dataMap.put("PSCacheAccessCount", dataSource.getCachedPreparedStatementAccessCount());
        dataMap.put("PSCacheHitCount", dataSource.getCachedPreparedStatementHitCount());
        dataMap.put("PSCacheMissCount", dataSource.getCachedPreparedStatementMissCount());

        dataMap.put("StartTransactionCount", dataSource.getStartTransactionCount());
        dataMap.put("TransactionHistogram", dataSource.getTransactionHistogramValues());

        dataMap.put("ConnectionHoldTimeHistogram",
                    dataSource.getDataSourceStat().getConnectionHoldHistogram().toArray());
        dataMap.put("RemoveAbandoned", dataSource.isRemoveAbandoned());

        return dataMap;
    }
}