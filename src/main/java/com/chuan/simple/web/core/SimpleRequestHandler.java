/*
 * Copyright 2018-2021 Bingchuan Sun.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chuan.simple.web.core;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.chuan.simple.helper.annotation.AnnotationAttributeHelper;
import com.chuan.simple.helper.common.StringHelper;
import com.chuan.simple.helper.generic.GenericTypeHelper;
import com.chuan.simple.helper.method.ParameterHelper;
import com.chuan.simple.web.annotation.JSONBody;
import com.chuan.simple.web.helper.RequestHelper;

public class SimpleRequestHandler {

    private final Log log = LogFactory.getLog(this.getClass());

    private final Method method;

    private final Object owner;

    public SimpleRequestHandler(Object owner, Method method) {
        this.owner = owner;
        this.method = method;
    }

    public void handle(HttpServletRequest request,
            HttpServletResponse response) {
        try {
            Object[] paramValues = buildParams(request, response);
            buildResponse(response, paramValues);
        } catch (Exception e) {
            log.info("Handle request " + request.getRequestURI() + " failed",
                    e);
        }
    }
    
    private void buildResponse(HttpServletResponse response,Object[] paramValues) throws Exception {
        Object result = method.invoke(owner, paramValues);
        if (result==null) {
            return;
        }
        if (AnnotationAttributeHelper.from(method).containsKey(JSONBody.class)) {
            response.setContentType("application/json;charset="+Charset.defaultCharset());
            response.getWriter().write(JSON.toJSONString(result));
        }
    }
    
    
    
    @SuppressWarnings("restriction")
    private Object[] buildParams(HttpServletRequest request,
            HttpServletResponse response) {     
        Object[] paramValues =new Object[method.getParameterCount()];
        Parameter[] parameters = ParameterHelper.getParameters(method);
        String[] paramNames = ParameterHelper.getParameterNames(method);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String paramName = paramNames[i];
            Class<?> parameterType = parameter.getType();
            Object paramValue = null;
            if (parameterType.isInstance(request)) {
                paramValue=request;
            }else if (parameterType.isInstance(response)) {
                paramValue=response;
            }else if(AnnotationAttributeHelper.from(parameter).containsKey(JSONBody.class)) {
                if (Collection.class.isAssignableFrom(parameterType)) {
                    Class<?> genericType = GenericTypeHelper.getGenericClass(parameter, Collection.class, 0);
                    if (genericType!=null) {
                        paramValue = RequestHelper.getRequestJsonArray(request, genericType);
                    }
                }else {
                    paramValue = RequestHelper.getRequestJsonObject(request, parameterType);
                }
            }else if(StringHelper.isNotEmpty(paramName)) {
                if(String.class.equals(parameterType)){
                    paramValue = request.getParameter(paramName);
                    if(paramValue==null) {
                        paramValue=request.getHeader(paramName);
                    }
                }else if(String[].class.equals(parameterType)) {
                    paramValue = request.getParameterValues(paramName);
                } 
            }
            if (paramValue!=null) {
                paramValues[i] = paramValue;
            }
        }
        return paramValues;
    }
    
}
