/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.plugin.access.pluggabletask;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskConfigProperty;
import com.thoughtworks.go.plugin.api.task.TaskView;
import com.thoughtworks.go.plugin.infra.PluginManager;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JsonBasedPluggableTaskTest {

    private PluginManager pluginManager;
    private JsonBasedPluggableTask task;
    private GoPluginApiResponse goPluginApiResponse;
    private String pluginId;

    @Before
    public void setup() {
        pluginManager = mock(PluginManager.class);
        pluginId = "plugin-id";
        task = new JsonBasedPluggableTask(pluginManager, pluginId, new JsonBasedTaskExtensionHandler_V1());
        goPluginApiResponse = mock(GoPluginApiResponse.class);
        when(pluginManager.submitTo(eq(pluginId), any(GoPluginApiRequest.class))).thenReturn(goPluginApiResponse);
    }

    @Test
    public void shouldGetTheTaskConfig() {

        String jsonResponse = "{" +
                "\"URL\":{\"default-value\":\"\",\"secure\":false,\"required\":true,\"display-name\":\"URL\",\"display-order\":\"0\"}," +
                "\"USER\":{\"default-value\":\"foo\",\"secure\":true,\"required\":true,\"display-name\":\"User\",\"display-order\":\"1\"}," +
                "\"PASSWORD\":{}" +
                "}";

        when(goPluginApiResponse.responseBody()).thenReturn(jsonResponse);

        TaskConfig config = task.config();

        Property url = config.get("URL");
        assertThat(url.getOption(Property.REQUIRED), is(true));
        assertThat(url.getOption(Property.SECURE), is(false));
        assertThat(url.getOption(Property.DISPLAY_NAME), is("URL"));
        assertThat(url.getOption(Property.DISPLAY_ORDER), is(0));

        Property user = config.get("USER");
        assertThat(user.getOption(Property.REQUIRED), is(true));
        assertThat(user.getOption(Property.SECURE), is(true));
        assertThat(user.getOption(Property.DISPLAY_NAME), is("User"));
        assertThat(user.getOption(Property.DISPLAY_ORDER), is(1));

        Property password = config.get("PASSWORD");
        assertThat(password.getOption(Property.REQUIRED), is(true));
        assertThat(password.getOption(Property.SECURE), is(false));
        assertThat(password.getOption(Property.DISPLAY_ORDER), is(0));
        assertThat(password.getOption(Property.DISPLAY_NAME), is(""));

        ArgumentCaptor<GoPluginApiRequest> argument = ArgumentCaptor.forClass(GoPluginApiRequest.class);
        verify(pluginManager).submitTo(eq(pluginId), argument.capture());
        MatcherAssert.assertThat(argument.getValue().extension(), Matchers.is(JsonBasedTaskExtension.TASK_EXTENSION));
        MatcherAssert.assertThat(argument.getValue().extensionVersion(), Matchers.is(JsonBasedTaskExtensionHandler_V1.VERSION));
        MatcherAssert.assertThat(argument.getValue().requestName(), Matchers.is(JsonBasedTaskExtension.CONFIGURATION_REQUEST));
    }

    @Test
    public void shouldGetTaskView() {
        String jsonResponse = "{\"displayValue\":\"MyTaskPlugin\", \"template\":\"<html>junk</html>\"}";
        when(goPluginApiResponse.responseBody()).thenReturn(jsonResponse);

        TaskView view = task.view();
        assertThat(view.displayValue(), is("MyTaskPlugin"));
        assertThat(view.template(), is("<html>junk</html>"));

        ArgumentCaptor<GoPluginApiRequest> argument = ArgumentCaptor.forClass(GoPluginApiRequest.class);
        verify(pluginManager).submitTo(eq(pluginId), argument.capture());
        MatcherAssert.assertThat(argument.getValue().extension(), Matchers.is(JsonBasedTaskExtension.TASK_EXTENSION));
        MatcherAssert.assertThat(argument.getValue().extensionVersion(), Matchers.is(JsonBasedTaskExtensionHandler_V1.VERSION));
        MatcherAssert.assertThat(argument.getValue().requestName(), Matchers.is(JsonBasedTaskExtension.TASK_VIEW_REQUEST));
    }

    @Test
    public void shouldValidateTaskConfig() {
        String jsonResponse = "{\"errors\":[{\"key\":\"key1\", \"message\":\"err1\"},{\"key\":\"key1\", \"message\":\"err2\"},{\"key\":\"key2\", \"message\":\"err3\"}]}";
        String config = "{\"URL\":{\"display-name\":\"URL\",\"secure\":false,\"value\":\"http://foo\",\"display-order\":\"0\",\"required\":true}}";

        when(goPluginApiResponse.responseBody()).thenReturn(jsonResponse);

        TaskConfig configuration = new TaskConfig();
        final TaskConfigProperty property = new TaskConfigProperty("URL", "http://foo");
        property.with(Property.SECURE, false);
        property.with(Property.REQUIRED, true);
        property.with(Property.DISPLAY_NAME, "URL");
        property.with(Property.DISPLAY_ORDER, 0);
        configuration.add(property);
        ValidationResult result = task.validate(configuration);

        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getErrors().get(0).getKey(), is("key1"));
        assertThat(result.getErrors().get(0).getMessage(), is("err1"));
        assertThat(result.getErrors().get(1).getKey(), is("key1"));
        assertThat(result.getErrors().get(1).getMessage(), is("err2"));
        assertThat(result.getErrors().get(2).getKey(), is("key2"));
        assertThat(result.getErrors().get(2).getMessage(), is("err3"));

        ArgumentCaptor<GoPluginApiRequest> argument = ArgumentCaptor.forClass(GoPluginApiRequest.class);
        verify(pluginManager).submitTo(eq(pluginId), argument.capture());
        assertThat(argument.getValue().requestBody(), is(config));

        MatcherAssert.assertThat(argument.getValue().extension(), Matchers.is(JsonBasedTaskExtension.TASK_EXTENSION));
        MatcherAssert.assertThat(argument.getValue().extensionVersion(), Matchers.is(JsonBasedTaskExtensionHandler_V1.VERSION));
        MatcherAssert.assertThat(argument.getValue().requestName(), Matchers.is(JsonBasedTaskExtension.VALIDATION_REQUEST));
    }

    @Test
    public void shouldGetTaskExecutor() {
        assertTrue(task.executor() instanceof JsonBasedTaskExecutor);
    }
}