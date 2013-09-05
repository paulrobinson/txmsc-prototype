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
import org.junit.Test;

import java.io.File;

/**
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class RootTransactionTest {

    @Before
    public void resetData() {

        ConfigParticipant.reset();
        clearLog();
    }

    @Test
    public void testSimple() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        ConfigParticipant configParticipant1 = new ConfigParticipant("1");
        ConfigParticipant configParticipant2 = new ConfigParticipant("2");
        ba1.add(configParticipant1);
        ba1.add(configParticipant2);

        configParticipant1.setNewValue("1", "newVal1");
        configParticipant2.setNewValue("2", "newVal2");

        Assert.assertEquals(null, ConfigParticipant.getPersistedValue("1"));
        Assert.assertEquals(null, ConfigParticipant.getPersistedValue("2"));

        ba1.commit();

        Assert.assertEquals("newVal1", ConfigParticipant.getPersistedValue("1"));
        Assert.assertEquals("newVal2", ConfigParticipant.getPersistedValue("2"));

    }

    @Test
    public void testRecovery() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        ConfigParticipant configParticipant1 = new ConfigParticipant("1");
        ConfigParticipant configParticipant2 = new ConfigParticipant("2", true);
        ba1.add(configParticipant1);
        ba1.add(configParticipant2);

        configParticipant1.setNewValue("1", "newVal1");
        configParticipant2.setNewValue("2", "newVal2");

        Assert.assertEquals(null, ConfigParticipant.getPersistedValue("1"));
        Assert.assertEquals(null, ConfigParticipant.getPersistedValue("2"));

        try {
            ba1.commit();
            Assert.fail("Should have simulated a crash by throwing an java.lang.Error");
        } catch (Error e) {
            //expected
        }

        Assert.assertEquals("newVal1", ConfigParticipant.getPersistedValue("1"));
        Assert.assertEquals(null, ConfigParticipant.getPersistedValue("2"));

        //Now Run recovery and check it worked....
        ConfigParticipantRecordTypeMap map = new ConfigParticipantRecordTypeMap();
        RecordTypeManager.manager().add(map);

        RecoverySetup.startRecovery();
        RecoverySetup.runRecoveryScan();

        Thread.sleep(5000);

        Assert.assertEquals("newVal1", ConfigParticipant.getPersistedValue("1"));
        Assert.assertEquals("newVal2", ConfigParticipant.getPersistedValue("2"));

        RecoverySetup.stopRecovery();
    }

    private void clearLog() {

        File objectStoreDir = new File(BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir() + "/ShadowNoFileLockStore/defaultStore/StateManager/RootTransaction");
        for (File record : objectStoreDir.listFiles()) {
            boolean result = record.delete();
            if (!result) {
                throw new RuntimeException("Unable to delete file: " + record.getAbsolutePath());
            }
        }
    }
}
