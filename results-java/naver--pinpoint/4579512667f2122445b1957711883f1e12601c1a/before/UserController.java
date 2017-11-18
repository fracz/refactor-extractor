/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUser(@RequestBody User user) {

        if (StringUtils.isEmpty(user.getUserId()) || StringUtils.isEmpty(user.getName())) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userId or name in params to creating user infomation");
            return result;
        }

        userService.insertUser(user);

        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "SUCCESS");
        return result;
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deletetUser(@RequestBody User user) {
        if (StringUtils.isEmpty(user.getUserId())) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userId in params to delete user");
            return result;
        }

        userService.deleteUser(user);

        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "SUCCESS");
        return result;
    }
}