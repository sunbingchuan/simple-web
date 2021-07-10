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

import java.util.Set;

import com.chuan.simple.bean.core.SimpleContext;
import com.chuan.simple.helper.common.ObjectHelper;
import com.chuan.simple.helper.resource.Resource;
import com.chuan.simple.helper.resource.ResourceHelper;

/**
 * Context for simple web which will load default config file locating in
 * {@link #defaultConfigLocation}.
 */
public class SimpleWebContext extends SimpleContext {

    public static final String defaultConfigLocation = "simple-web.sp";

    private static SimpleWebContext defaultSimpleWebContext;

    public static SimpleWebContext getDefaultSimpleWebContext() {
        if (defaultSimpleWebContext == null) {
        	synchronized (SimpleWebContext.class) {
        		if (defaultSimpleWebContext == null) {
            Set<Resource> resources =
                    ResourceHelper.resources(defaultConfigLocation);
            if (!ObjectHelper.isEmpty(resources)) {
                defaultSimpleWebContext = new SimpleWebContext();
                defaultSimpleWebContext
                        .addConfig(resources.toArray(new Resource[0]));
                defaultSimpleWebContext.refresh();
		            }
				}
            }
        }
        return defaultSimpleWebContext;
    }

    public SimpleWebContext() {
        super();
    }

    public SimpleWebContext(String... configs) {
        super(configs);
    }

    public static synchronized void setDefaultSimpleWebContext(
            SimpleWebContext defaultSimpleWebContext) {
        SimpleWebContext.defaultSimpleWebContext = defaultSimpleWebContext;
    }

}
