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

public class Constants {
    public static final String GENERATED_CLASS_PACKAGE = "org.fakereplace.proxies";

    public static final String ADDED_METHOD_NAME = "______REDEFINED_METHOD_DELEGATOR_$";

    public static final String ADDED_METHOD_DESCRIPTOR = "(I[Ljava/lang/Object;)Ljava/lang/Object;";

    public static final String ADDED_STATIC_METHOD_NAME = "______REDEFINED_STATIC_METHOD_DELEGATOR_$";

    public static final String ADDED_CONSTRUCTOR_DESCRIPTOR = "(I[Ljava/lang/Object;Lorg/fakereplace/core/ConstructorArgument;)V";

    public static final String FINAL_METHOD_ATTRIBUTE = "org.fakereplace.final";

}
