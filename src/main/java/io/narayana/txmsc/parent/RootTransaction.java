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
 * Need to extend BasicAction as BasicAction's Begin and End methods are protected.
 * <p/>
 * Why can't BasicAction be made non-abstract
 * Can we use TwoPhaseCoordinator?
 * Yes, but supports nesting which could cause problems.
 * Why can't we just use AtomicAction for this?
 * It does thread association that we don't need.
 * Does this really matter, currently looks like it will save a lot of code as AtomicActionRecoveryModule can't
 * easily be extended.
 *
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class RootTransaction extends BasicAction {

    public RootTransaction() {

        super(ActionType.TOP_LEVEL);
    }

    public RootTransaction(Uid objUid) {

        super(objUid, ActionType.TOP_LEVEL);
    }

    public int begin() {

        return super.Begin(null);
    }

    public int commit() {

        return super.End(true);
    }

    public int rollback() {

        return super.Abort();
    }

    public String type() {

        return "/StateManager/RootTransaction";
    }
}
