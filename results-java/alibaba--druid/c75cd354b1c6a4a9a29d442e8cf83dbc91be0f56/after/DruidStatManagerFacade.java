package com.alibaba.druid.stat;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.VERSION;
import com.alibaba.druid.util.DruidDataSourceUtils;
import com.alibaba.druid.util.JdbcSqlStatUtils;

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

    private DruidStatManagerFacade(){
    }

    public static DruidStatManagerFacade getInstance() {
        return instance;
    }

    private Set<Object> getDruidDataSourceInstances() {
        return DruidDataSourceStatManager.getInstances().keySet();
    }

    public void resetDataSourceStat() {
        DruidDataSourceStatManager.getInstance().reset();
    }

    public void resetSqlStat() {
        JdbcStatManager.getInstance().reset();
    }

    public void resetAll() {
        resetSqlStat();
        resetDataSourceStat();
    }

    public Object getSqlStatById(Integer id) {
        for (Object ds : getDruidDataSourceInstances()) {
            Object sqlStat = DruidDataSourceUtils.getSqlStat(ds, id);
            if (sqlStat != null) {
                return sqlStat;
            }
        }
        return null;
    }

    public Map<String, Object> getDataSourceStatData(Integer id) {
        if (id == null) {
            return null;
        }

        Object datasource = getDruidDataSourceById(id);
        return datasource == null ? null : dataSourceToMapData(datasource);
    }

    public Object getDruidDataSourceById(Integer identity) {
        if (identity == null) {
            return null;
        }

        for (Object datasource : getDruidDataSourceInstances()) {
            if (System.identityHashCode(datasource) == identity) {
                return datasource;
            }
        }
        return null;
    }

    public List<Map<String, Object>> getSqlStatDataList() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Object datasource : getDruidDataSourceInstances()) {
            for (Object sqlStat : DruidDataSourceUtils.getSqlStatMap(datasource).values()) {

                Map<String, Object> data = JdbcSqlStatUtils.getData(sqlStat);

                long executeCount = (Long) data.get("ExecuteCount");
                long runningCount = (Long) data.get("RunningCount");

                if (executeCount == 0 && runningCount == 0) {
                    continue;
                }

                result.add(data);
            }
        }
        return result;
    }

    public Map<String, Object> getSqlStatData(Integer id) {
        if (id == null) {
            return null;
        }

        Object sqlStat = getSqlStatById(id);

        if (sqlStat == null) {
            return null;
        }

        return JdbcSqlStatUtils.getData(sqlStat);
    }

    public List<Object> getDataSourceStatList() {
        List<Object> datasourceList = new ArrayList<Object>();
        for (Object dataSource : getDruidDataSourceInstances()) {
            datasourceList.add(dataSourceToMapData(dataSource));
        }
        return datasourceList;
    }

    public Map<String, Object> returnJSONBasicStat() {
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
        Object datasource = getDruidDataSourceById(id);

        if (datasource == null) {
            return null;
        }

        return DruidDataSourceUtils.getPoolingConnectionInfo(datasource);
    }

    public List<String> getActiveConnectionStackTraceByDataSourceId(Integer id) {
        Object datasource = getDruidDataSourceById(id);

        if (datasource == null || DruidDataSourceUtils.isRemoveAbandoned(datasource)) {
            return null;
        }

        return DruidDataSourceUtils.getActiveConnectionStackTrace(datasource);
    }

    private Map<String, Object> dataSourceToMapData(Object dataSource) {

        return DruidDataSourceUtils.getStatData(dataSource);
    }
}