package events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

public class Data extends BEvent {
    public Data(String aName, Object someData) {
        super(aName, someData);
    }
    @Override
    public String toString() {
        return "Data";
    }
}
