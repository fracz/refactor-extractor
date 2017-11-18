package com.alibaba.druid.bvt.support.http;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.springframework.mock.web.MockServletConfig;

import com.alibaba.druid.support.JSONDruidStatService;
import com.alibaba.druid.support.http.StatViewServlet;

public class StatViewServletTest_resetEnable extends TestCase {

    protected void setUp() throws Exception {
        JSONDruidStatService.getInstance().setResetEnable(true);
    }

    protected void tearDown() throws Exception {
        JSONDruidStatService.getInstance().setResetEnable(true);
    }

    public void test_resetEnable_none() throws Exception {
        Assert.assertTrue(JSONDruidStatService.getInstance().isResetEnable());

        MockServletConfig servletConfig = new MockServletConfig();

        StatViewServlet servlet = new StatViewServlet();
        servlet.init(servletConfig);

        Assert.assertTrue(JSONDruidStatService.getInstance().isResetEnable());
    }

    public void test_resetEnable_true() throws Exception {
        Assert.assertTrue(JSONDruidStatService.getInstance().isResetEnable());

        MockServletConfig servletConfig = new MockServletConfig();
        servletConfig.addInitParameter(StatViewServlet.PARAM_NAME_RESET_ENABLE, "true");

        StatViewServlet servlet = new StatViewServlet();
        servlet.init(servletConfig);

        Assert.assertTrue(JSONDruidStatService.getInstance().isResetEnable());
    }

    public void test_resetEnable_empty() throws Exception {
        Assert.assertTrue(JSONDruidStatService.getInstance().isResetEnable());

        MockServletConfig servletConfig = new MockServletConfig();
        servletConfig.addInitParameter(StatViewServlet.PARAM_NAME_RESET_ENABLE, "");

        StatViewServlet servlet = new StatViewServlet();
        servlet.init(servletConfig);

        Assert.assertTrue(JSONDruidStatService.getInstance().isResetEnable());
    }

    public void test_resetEnable_false() throws Exception {
        Assert.assertTrue(JSONDruidStatService.getInstance().isResetEnable());

        MockServletConfig servletConfig = new MockServletConfig();
        servletConfig.addInitParameter(StatViewServlet.PARAM_NAME_RESET_ENABLE, "false");

        StatViewServlet servlet = new StatViewServlet();
        servlet.init(servletConfig);

        Assert.assertFalse(JSONDruidStatService.getInstance().isResetEnable());
    }

    public void test_resetEnable_error() throws Exception {
        Assert.assertTrue(JSONDruidStatService.getInstance().isResetEnable());

        MockServletConfig servletConfig = new MockServletConfig();
        servletConfig.addInitParameter(StatViewServlet.PARAM_NAME_RESET_ENABLE, "xxx");

        StatViewServlet servlet = new StatViewServlet();
        servlet.init(servletConfig);

        Assert.assertFalse(JSONDruidStatService.getInstance().isResetEnable());
    }
}