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
import io.narayana.txmsc.transport.ProxyBasicRecord;
import org.junit.Test;

/**
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class SubordinateTest {

    @Test
    public void testSimple() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        DummyBasicRecord dummyBasicRecord1 = new DummyBasicRecord("1");
        DummyBasicRecord dummyBasicRecord2 = new DummyBasicRecord("2");
        ba1.add(dummyBasicRecord1);
        ba1.add(dummyBasicRecord2);

        Integer serverId = 1;
        Uid rootTransactionUid = new Uid();
        SubordinateTransaction subordinateTransaction = BasicActionImporter.getInstance().getSubordinateTransaction(serverId, rootTransactionUid);
        subordinateTransaction.begin();
        ProxyBasicRecord proxyBasicRecord = new ProxyBasicRecord("proxy", subordinateTransaction);

        ba1.add(proxyBasicRecord);

        DummyBasicRecord dummySubRecord1 = new DummyBasicRecord("sub-1");
        DummyBasicRecord dummySubRecord2 = new DummyBasicRecord("sub-2");
        subordinateTransaction.add(dummySubRecord1);
        subordinateTransaction.add(dummySubRecord2);

        ba1.commit();

    }
}
