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
import com.arjuna.ats.arjuna.coordinator.ActionType;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.io.IOException;

/**
 * Represents the subordinate transaction running on the child-side.
 *
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class SubordinateTransaction extends BasicAction {

    private Integer serverId;

    public SubordinateTransaction() {

    }

    /**
     * Creates a new instance of a new Subordinate Transaction. This instance can be used once-only. After the transaction
     * terminates it must not be re-used to begin a new transaction.
     *
     * @param serverId the ID of the parent that initiated the root transaction.
     */
    public SubordinateTransaction(Integer serverId) {

        super(ActionType.TOP_LEVEL);
        this.serverId = serverId;
    }

    /**
     * Re-create an existing Subordinate Transaction, re-loading it from the object store.
     *
     * @param uid
     */
    public SubordinateTransaction(Uid uid) {

        super(uid);
        activate();
    }

    /**
     * Returns ths the identifier for transactions of this type.
     *
     * @return
     */
    public String type() {

        return "/StateManager/SubordinateTransaction";
    }

    /**
     * Get the Server ID of the parent who initiated the root transaction.
     *
     * @return The server ID.
     */
    public Integer getServerId() {

        return serverId;
    }

    /**
     * Serialise the state of the transaction, ready for it being written to the object-store.
     *
     * @param os the OutputObjectStream to write the state to.
     * @param i
     * @return boolean representing success/failure.
     */
    @Override
    public boolean save_state(OutputObjectState os, int i) {

        try {
            os.packInt(serverId);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!super.save_state(os, i)) {
            return false;
        }

        return true;
    }

    /**
     * Called when restoring the state of the transaction from the object-store during recovery.
     *
     * @param os the InputObjectState to read the state from.
     * @param i
     * @return boolean representing success/failure.
     */
    @Override
    public boolean restore_state(InputObjectState os, int i) {

        try {
            serverId = os.unpackInt();
        } catch (IOException e) {
            return false;
        }

        if (!super.restore_state(os, i)) {
            return false;
        }

        return true;
    }

    /**
     * Begin the Subordinate Transaction.
     *
     * @return the status of the operation.
     */
    public int begin() {

        return super.Begin(null);
    }

    /**
     * Prepare the Subordinate Transaction
     *
     * @return the status of the operation.
     */
    public int prepare() {

        return super.prepare(true);
    }

    /**
     * commit the Subordinate Transaction
     *
     * @return the status of the operation.
     */
    public int commit() {

        super.phase2Commit(true);
        return super.status();
    }

    /**
     * Rollback the Subordinate Transaction
     *
     * @return the status of the operation.
     */
    public int rollback() {

        super.phase2Abort(true);
        return super.status();
    }


}
