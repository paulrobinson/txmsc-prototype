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
import com.arjuna.ats.arjuna.coordinator.ActionType;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.io.IOException;

/**
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class SubordinateTransaction extends BasicAction {

    private Integer serverId;

    public SubordinateTransaction(Integer serverId) {

        super(ActionType.TOP_LEVEL);
        this.serverId = serverId;
    }

    public SubordinateTransaction(Integer serverId, Uid uid) {

        super(uid);
        activate();
        this.serverId = serverId;
    }

    public String type() {

        return "/StateManager/SubordinateTransaction";
    }

    @Override
    public boolean save_state(OutputObjectState os, int i) {

        if (!super.save_state(os, i)) {
            return false;
        }

        try {
            os.packInt(serverId);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean restore_state(InputObjectState os, int i) {

        if (!super.restore_state(os, i)) {
            return false;
        }

        try {
            serverId = os.unpackInt();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public int begin() {

        return super.Begin(null);
    }

    public int prepare() {

        return super.prepare(true);
    }

    public int commit() {

        super.phase2Commit(true);
        return super.status();
    }

    public int rollback() {

        super.phase2Abort(true);
        return super.status();
    }


}
