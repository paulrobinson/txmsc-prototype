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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import io.narayana.txmsc.transport.ProxyBasicRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class RootTransactionTest {

    @Before
    public void resetData() {
        DummyBasicRecord.reset();
        RecoverySetup.clearLog();
    }

    @Test
    public void testSimple() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        DummyBasicRecord dummyBasicRecord1 = new DummyBasicRecord("1");
        DummyBasicRecord dummyBasicRecord2 = new DummyBasicRecord("2");
        ba1.add(dummyBasicRecord1);
        ba1.add(dummyBasicRecord2);

        dummyBasicRecord1.setNewValue("1", "newVal1");
        dummyBasicRecord2.setNewValue("2", "newVal2");

        Assert.assertEquals(null, DummyBasicRecord.getPersistedValue("1"));
        Assert.assertEquals(null, DummyBasicRecord.getPersistedValue("2"));

        ba1.commit();

        Assert.assertEquals("newVal1", DummyBasicRecord.getPersistedValue("1"));
        Assert.assertEquals("newVal2", DummyBasicRecord.getPersistedValue("2"));

    }

    @Test
    public void testRecovery() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        DummyBasicRecord dummyBasicRecord1 = new DummyBasicRecord("1");
        DummyBasicRecord dummyBasicRecord2 = new DummyBasicRecord("2", true);
        ba1.add(dummyBasicRecord1);
        ba1.add(dummyBasicRecord2);

        dummyBasicRecord1.setNewValue("1", "newVal1");
        dummyBasicRecord2.setNewValue("2", "newVal2");

        Assert.assertEquals(null, DummyBasicRecord.getPersistedValue("1"));
        Assert.assertEquals(null, DummyBasicRecord.getPersistedValue("2"));

        try {
            ba1.commit();
            Assert.fail("Should have simulated a crash by throwing an java.lang.Error");
        } catch (Error e) {
            //expected
        }

        Assert.assertEquals("newVal1", DummyBasicRecord.getPersistedValue("1"));
        Assert.assertEquals(null, DummyBasicRecord.getPersistedValue("2"));

        //Now Run recovery and check it worked....
        DummyBasicRecordTypeMap map = new DummyBasicRecordTypeMap();
        RecordTypeManager.manager().add(map);

        RecoverySetup.startRecovery();
        RecoverySetup.runRecoveryScan();

        Thread.sleep(5000);

        Assert.assertEquals("newVal1", DummyBasicRecord.getPersistedValue("1"));
        Assert.assertEquals("newVal2", DummyBasicRecord.getPersistedValue("2"));
    }

    @Test
    public void testRecovery2() throws Exception {

        runTransaction();
        recoverTransaction();
    }

    private static void runTransaction() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        DummyBasicRecord dummyBasicRecord1 = new DummyBasicRecord("1");
        DummyBasicRecord dummyBasicRecord2 = new DummyBasicRecord("2", true);
        ba1.add(dummyBasicRecord1);
        ba1.add(dummyBasicRecord2);

        dummyBasicRecord1.setNewValue("1", "newVal1");
        dummyBasicRecord2.setNewValue("2", "newVal2");

        try {
            ba1.commit();
        } catch (Error e) {
            System.out.println("Server crashed, as expected");
        }
    }

    private static void recoverTransaction() throws Exception {

        DummyBasicRecordTypeMap map = new DummyBasicRecordTypeMap();
        RecordTypeManager.manager().add(map);

        RecoverySetup.startRecovery();
        RecoverySetup.runRecoveryScan();

        Thread.sleep(5000);

        System.out.println(DummyBasicRecord.getPersistedValue("1"));
        System.out.println(DummyBasicRecord.getPersistedValue("2"));
    }
}
