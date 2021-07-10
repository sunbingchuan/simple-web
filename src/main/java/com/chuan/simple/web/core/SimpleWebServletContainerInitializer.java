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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.chuan.simple.bean.annotation.Autowired;
import com.chuan.simple.bean.core.build.builder.Builder;
import com.chuan.simple.bean.core.element.entity.FieldElement;
import com.chuan.simple.bean.core.info.AnnotationInfo;
import com.chuan.simple.bean.core.info.ClassInfo;
import com.chuan.simple.bean.core.info.MethodInfo;
import com.chuan.simple.constant.Constant;
import com.chuan.simple.helper.annotation.AnnotationAttribute;
import com.chuan.simple.helper.annotation.AnnotationAttributeHelper;
import com.chuan.simple.helper.clazz.ClassHelper;
import com.chuan.simple.helper.method.MethodHelper;
import com.chuan.simple.helper.resource.PathHelper;
import com.chuan.simple.web.annotation.Request;
import com.chuan.simple.web.annotation.Servlet;

/**
 * Simple-bean web {@code ServletContainerInitializer}
 * @see ServletContainerInitializer
 */
public class SimpleWebServletContainerInitializer
        implements ServletContainerInitializer {

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void onStartup(Set<Class<?>> types, ServletContext context)
            throws ServletException {
        SimpleWebContext simpleWebContext =
                SimpleWebContext.getDefaultSimpleWebContext();
        if (simpleWebContext == null) {
            log.debug(
                    "SimpleWebContext is not configured,stop initializing simple-web");
            return;
        }
        FilterMapper filterMapper =
                simpleWebContext.tryBuild(FilterMapper.class);
        SimpleWebFilter simpleWebFilter = initSimpleWebFilter(simpleWebContext);
        Dynamic simpleDynamic =
                context.addFilter("simple-web", simpleWebFilter);
        if (filterMapper == null) {
            filterMapper = new FilterMapper() {
                @Override
                public void map(Dynamic simpleDynamic) {
                    simpleDynamic.addMappingForUrlPatterns(
                            EnumSet.of(DispatcherType.REQUEST), true, "/*");
                }
            };
        }
        filterMapper.map(simpleDynamic);
    }

    @SuppressWarnings("unchecked")
    private SimpleWebFilter initSimpleWebFilter(
            SimpleWebContext simpleWebContext) {
        SimpleWebFilter simpleWebFilter = new SimpleWebFilter();
        for (Builder<?> builder : simpleWebContext.getBuilders()) {
            Class<?> builderClass = builder.getBuilderClass();
            Map<Class<? extends Annotation>, AnnotationAttribute> attrs =
                    AnnotationAttributeHelper.from(builderClass);
            AnnotationAttribute annotationAttribute = attrs.get(Servlet.class);
            if (annotationAttribute == null) {
                continue;
            }
            for (Method method : MethodHelper.getMethods(builderClass, false)) {
                Map<Class<? extends Annotation>, AnnotationAttribute> methodAttrs =
                        AnnotationAttributeHelper.from(method);
                AnnotationAttribute requestAttribute = methodAttrs.get(Request.class);
                if (requestAttribute == null) {
                    continue;
                }
                String[] paths = (String[]) requestAttribute.getAttribute(Constant.ATTR_VALUE);
                try {
                    Object bean =
                            simpleWebContext.build(builder.getBuilderName());
                    SimpleRequestHandler handler =
                            new SimpleRequestHandler(bean, method);
                    for (String path : paths) {
                        if (!path.startsWith(PathHelper.FOLDER_SEPARATOR)) {
                            path = PathHelper.FOLDER_SEPARATOR + path;
                        }
                        path = PathHelper.cleanPath(path);
                        simpleWebFilter.addHandler(path, handler);
                    }
                } catch (Exception e) {
                    log.error("Initialize request handler of path " + paths
                            + " failed", e);
                }
            }
        }
        return simpleWebFilter;
    }

}
