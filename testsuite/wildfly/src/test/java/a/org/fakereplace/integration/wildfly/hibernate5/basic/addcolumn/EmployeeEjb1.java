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

package a.org.fakereplace.integration.wildfly.hibernate5.basic.addcolumn;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Stuart Douglas
 */
@Stateless
public class EmployeeEjb1 implements RemoteEmployee {

    @PersistenceContext
    private EntityManager entityManager;

    public void saveEntity(int id) {
        final Employee1 employee = new Employee1();
        employee.setId(id);
        employee.setName("name");
        employee.setAddress("address");
        entityManager.persist(employee);
    }

    @Override
    public String getEntityDesc(final int id) {
        return entityManager.find(Employee.class, id).toString();
    }
}
