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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chuan.simple.helper.common.PatternHelper;
import com.chuan.simple.helper.resource.PathHelper;

/**
 * Filter the request match simple bean web servlet.
 */
public class SimpleWebFilter implements Filter {

    private final Map<String, SimpleRequestHandler> handlers =
            new HashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest
                && response instanceof HttpServletResponse) {
            SimpleRequestHandler handler =
                    getHandler((HttpServletRequest) request);
            if (handler != null) {
                handler.handle((HttpServletRequest) request,
                        (HttpServletResponse) response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private SimpleRequestHandler getHandler(HttpServletRequest request) {
        SimpleRequestHandler handler = null;
        String requestUri = PathHelper.cleanPath(request.getRequestURI());
        for (Entry<String, SimpleRequestHandler> entry : handlers.entrySet()) {
            if (PatternHelper.matchPath(
                    request.getContextPath() + entry.getKey(), requestUri)) {
                handler = entry.getValue();
                break;
            }
        }
        return handler;
    }

    @Override
    public void destroy() {
    }

    public Map<String, SimpleRequestHandler> getHandlers() {
        return handlers;
    }

    public void addHandler(String uri, SimpleRequestHandler handler) {
        this.handlers.put(uri, handler);
    }

}
