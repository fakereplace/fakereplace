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

package org.fakereplace.core;

import java.util.List;

import org.fakereplace.Extension;
import org.fakereplace.transformation.FakereplaceTransformer;

/**
 * @author Stuart Douglas
 */
public interface InternalExtension extends Extension {


    /**
     * Integrations have a change to transform classes
     * They get to see the class before any manipulation is
     * done to it.
     * They do not get to transform reloaded classes.
     *
     */
    List<FakereplaceTransformer> getTransformers();
}
