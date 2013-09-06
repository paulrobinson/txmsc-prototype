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
import io.narayana.txmsc.child.SubordinateTransaction;
import io.narayana.txmsc.parent.NodeConfig;
import io.narayana.txmsc.parent.RootTransaction;
import io.narayana.txmsc.parent.SubordinateParticipantStub;
import io.narayana.txmsc.child.SubordinateTransactionImporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class SubordinateTest {

    @Before
    public void resetData() {

        ConfigService.reset();
    }

    @Test
    public void testSimple() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        ConfigService configService1 = new ConfigService("1");
        ConfigService configService2 = new ConfigService("2");
        ba1.add(configService1.getParticipant());
        ba1.add(configService2.getParticipant());

        configService1.setNewValue("1", "newVal1");
        configService2.setNewValue("2", "newVal2");

        Assert.assertEquals(null, ConfigService.getPersistedValue("1"));
        Assert.assertEquals(null, ConfigService.getPersistedValue("2"));



        SubordinateTransaction subordinateTransaction = SubordinateTransactionImporter.getSubordinateTransaction(NodeConfig.SERVER_ID, null);
        subordinateTransaction.begin();
        //Get subordinate Uid and pass to parent.
        Uid subordinateUid = subordinateTransaction.get_uid();

        SubordinateParticipantStub subordinateParticipantStub = new SubordinateParticipantStub(NodeConfig.SERVER_ID, subordinateUid);
        ba1.add(subordinateParticipantStub);


        ConfigService dummySubRecord1 = new ConfigService("sub-1");
        ConfigService dummySubRecord2 = new ConfigService("sub-2");
        subordinateTransaction.add(dummySubRecord1.getParticipant());
        subordinateTransaction.add(dummySubRecord2.getParticipant());

        dummySubRecord1.setNewValue("sub-1", "sub-newVal1");
        dummySubRecord2.setNewValue("sub-2", "sub-newVal2");

        Assert.assertEquals(null, ConfigService.getPersistedValue("sub-1"));
        Assert.assertEquals(null, ConfigService.getPersistedValue("sub-2"));

        ba1.commit();

        Assert.assertEquals("newVal1", ConfigService.getPersistedValue("1"));
        Assert.assertEquals("newVal2", ConfigService.getPersistedValue("2"));
        Assert.assertEquals("sub-newVal1", ConfigService.getPersistedValue("sub-1"));
        Assert.assertEquals("sub-newVal2", ConfigService.getPersistedValue("sub-2"));

    }
}
