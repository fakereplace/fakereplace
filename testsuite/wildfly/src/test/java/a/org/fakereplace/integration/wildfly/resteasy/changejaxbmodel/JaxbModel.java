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

package a.org.fakereplace.integration.wildfly.resteasy.changejaxbmodel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Stuart Douglas
 */
@XmlRootElement
public class JaxbModel {

    private String first;

    public JaxbModel(String first) {
        this.first = first;
    }

    public JaxbModel() {

    }

    public String getFirst() {
        return first;
    }


    public void setFirst(String first) {
        this.first = first;
    }

}
