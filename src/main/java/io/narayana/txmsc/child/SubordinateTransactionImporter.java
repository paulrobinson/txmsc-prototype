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

package io.narayana.txmsc.child;

import com.arjuna.ats.arjuna.common.Uid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for caching Subordinate Transactions in memory.
 *
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class SubordinateTransactionImporter {

    /**
     * A collection of Subordinate Transactions keyed on their Uid and the ServerId that initiated the Root Transaction.
     */
    private static Map<Integer, Map<Uid, SubordinateTransaction>> transactions = new ConcurrentHashMap<Integer, Map<Uid, SubordinateTransaction>>();

    /**
     * Create a new Subordinate Transaction and store it in the cache.
     *
     * @param serverId The server ID of the server that initiated the transaction.
     * @return The newly created Subordinate Transaction
     */
    public static SubordinateTransaction createSubordinateTransaction(Integer serverId) {

        if (serverId == null)
            throw new IllegalArgumentException();

        Map<Uid, SubordinateTransaction> subordinates = transactions.get(serverId);
        if (subordinates == null) {
            subordinates = new ConcurrentHashMap<Uid, SubordinateTransaction>();
            transactions.put(serverId, subordinates);
        }

        SubordinateTransaction imported = new SubordinateTransaction(serverId);
        subordinates.put(imported.get_uid(), imported);

        return imported;
    }


    /**
     * Lookup an existing Subordinate transaction.
     *
     * @param serverId
     * @param subordinateUid
     * @return
     */
    public static SubordinateTransaction getSubordinateTransaction(Integer serverId, Uid subordinateUid) {

        if (serverId == null || subordinateUid == null)
            throw new IllegalArgumentException();

        Map<Uid, SubordinateTransaction> subordinates = transactions.get(serverId);
        if (subordinates == null) {
            return null;
        }

        return subordinates.get(subordinateUid);
    }

    public static Collection<SubordinateTransaction> getSubordinateTransactions(Integer serverId) {

        Map<Uid, SubordinateTransaction> subordinateTransactionMap = transactions.get(serverId);
        if (subordinateTransactionMap == null) {
            return null;
        }

        return subordinateTransactionMap.values();
    }

}
