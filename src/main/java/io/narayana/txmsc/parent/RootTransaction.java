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

package io.narayana.txmsc.parent;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionType;
import com.arjuna.ats.arjuna.coordinator.BasicAction;

/**
 * Represents the Root Transaction. Extension of BasicAction is used to expose some of the underlying protected 
 * methods.
 *
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class RootTransaction extends BasicAction {

    /**
     * Creates a new instance of a Root Transaction. This instance can be used once-only. After the transaction
     * terminates it must not be re-used to begin a new transaction.
     *
     */
    public RootTransaction() {

        super(ActionType.TOP_LEVEL);
    }
    /**
     * Re-create an existing Root Transaction, re-loading it from the object store.
     *
     * @param uid
     */
    public RootTransaction(Uid uid) {

        //Pass in ActionType.TOP_LEVEL to ensure that nesting is not attempted.
        super(uid, ActionType.TOP_LEVEL);
    }

    /**
     * Begin the Root Transaction.
     *
     * @return the status of the operation.
     */
    public int begin() {

        return super.Begin(null);
    }

    /**
     * Commit the Root Transaction.
     *
     * @return the status of the operation.
     */
    public int commit() {

        return super.End(true);
    }

    /**
     * Rollback the Subordinate Transaction.
     *
     * @return the status of the operation.
     */
    public int rollback() {

        return super.Abort();
    }

    /**
     * Returns the identifier for transactions of this type.
     *
     * @return
     */
    public String type() {

        return "/StateManager/RootTransaction";
    }
}
