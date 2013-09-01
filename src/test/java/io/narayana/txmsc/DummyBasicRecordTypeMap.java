package io.narayana.txmsc;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;

/**
 * @author paul.robinson@redhat.com 01/09/2013
 */
public class DummyBasicRecordTypeMap implements RecordTypeMap {

    @Override
    public Class<? extends AbstractRecord> getRecordClass() {

        return DummyBasicRecord.class;
    }

    @Override
    public int getType() {

        return RecordType.USER_DEF_FIRST8;
    }
}
