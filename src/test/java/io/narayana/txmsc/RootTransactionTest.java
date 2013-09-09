/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package io.narayana.txmsc;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import io.narayana.txmsc.parent.RootTransaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Test just the Root Transaction, without any Subordinate transactions registered.
 *
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class RootTransactionTest {

    @Before
    public void resetData() {

        ConfigService.reset();
    }

    @Test
    public void testSimple() throws Exception {

        RootTransaction rootTransaction = new RootTransaction();
        rootTransaction.begin();

        ConfigService configService1 = new ConfigService("1");
        ConfigService configService2 = new ConfigService("2");
        rootTransaction.add(configService1);
        rootTransaction.add(configService2);

        configService1.setNewValue("1", "newVal1");
        configService2.setNewValue("2", "newVal2");

        Assert.assertEquals(null, ConfigService.getCommittedValue("1"));
        Assert.assertEquals(null, ConfigService.getCommittedValue("2"));

        rootTransaction.commit();

        Assert.assertEquals("newVal1", ConfigService.getCommittedValue("1"));
        Assert.assertEquals("newVal2", ConfigService.getCommittedValue("2"));

    }

}
