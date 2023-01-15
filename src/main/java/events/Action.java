package events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

public class Action extends BEvent {
    public Action(String aName) {
        super(aName);
    }
    @Override
    public String toString() {
        return this.name;
    }
}
