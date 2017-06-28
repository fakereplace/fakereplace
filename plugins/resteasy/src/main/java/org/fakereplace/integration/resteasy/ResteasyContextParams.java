/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.fakereplace.integration.resteasy;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;

/**
 * @author Stuart Douglas
 */
public class ResteasyContextParams {

    public static Set<String> init(final ServletContext context, final Set<String> existing) {
        if(existing != null) {
            return existing;
        }
        final Set<String> ret = new HashSet<String>();
        Enumeration names = context.getAttributeNames();
        while (names.hasMoreElements()) {
            ret.add((String) names.nextElement());
        }
        return ret;
    }
}
