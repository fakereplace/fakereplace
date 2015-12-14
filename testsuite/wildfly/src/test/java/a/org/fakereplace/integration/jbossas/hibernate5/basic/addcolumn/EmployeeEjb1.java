/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package a.org.fakereplace.integration.jbossas.hibernate5.basic.addcolumn;

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
