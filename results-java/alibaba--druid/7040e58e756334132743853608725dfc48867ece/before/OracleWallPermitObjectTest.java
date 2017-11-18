package com.alibaba.druid.bvt.filter.wall;

import junit.framework.TestCase;

import org.junit.Assert;

import com.alibaba.druid.filter.wall.WallConfig;
import com.alibaba.druid.filter.wall.WallUtils;

/**
 * 这个场景测试访问Oracle系统对象
 *
 * @author admin
 */
public class OracleWallPermitObjectTest extends TestCase {

    public void test_permitTable() throws Exception {
        Assert.assertFalse(WallUtils.isValidateOracle("select  sys.LinxReadFile('c:/boot.ini') from dual"));
        Assert.assertFalse(WallUtils.isValidateOracle("select  sys.LinxRunCMD('cmd /c net user linx /add') from dual"));
        Assert.assertFalse(WallUtils.isValidateOracle("select utl_inaddr.get_host_address from DUAL"));
        Assert.assertFalse(WallUtils.isValidateOracle("select TO_CHAR(utl_inaddr.get_host_address) from DUAL"));
        Assert.assertFalse(WallUtils.isValidateOracle("SELECT SYS.DBMS_EXPORT_EXTENSION.GET_DOMAIN_INDEX_TABLES('FOO','BAR','DBMS_OUTPUT'.PUT(:P1));"));
        Assert.assertFalse(WallUtils.isValidateOracle("select SYS.DBMS_EXPORT_EXTENSION.GET_DOMAIN_INDEX_TABLES()"));
    }

    public void test_permitTable_allow() throws Exception {
        WallConfig config = new WallConfig();
        config.setObjectCheck(false);
        Assert.assertTrue(WallUtils.isValidateOracle("select  sys.LinxReadFile('c:/boot.ini') from dual", config));
        Assert.assertTrue(WallUtils.isValidateOracle("select  sys.LinxRunCMD('cmd /c net user linx /add') from dual", config));
        Assert.assertTrue(WallUtils.isValidateOracle("select utl_inaddr.get_host_address from DUAL", config));
        Assert.assertTrue(WallUtils.isValidateOracle("select TO_CHAR(utl_inaddr.get_host_address) from DUAL", config));
        Assert.assertTrue(WallUtils.isValidateOracle("SELECT SYS.DBMS_EXPORT_EXTENSION.GET_DOMAIN_INDEX_TABLES('FOO','BAR','DBMS_OUTPUT'.PUT(:P1));", config));
        Assert.assertTrue(WallUtils.isValidateOracle("select SYS.DBMS_EXPORT_EXTENSION.GET_DOMAIN_INDEX_TABLES()", config));
    }
}