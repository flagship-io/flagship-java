package com.abtasty.flagship.hits;

import com.abtasty.flagship.utils.FlagshipConstants;

public class Event extends Hit<Event> {

    public enum EventCategory {

        ACTION_TRACKING("Action Tracking"),
        USER_ENGAGEMENT("User Engagement");

        private String name = null;

        EventCategory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Hit which represents an event. Can be a anything you want :  for example a click or a newsletter subscription.
     *
     * @param category category of the event (ACTION_TRACKING or USER_ENGAGEMENT) @required
     * @param action the event action @required
     */
    public Event(EventCategory category, String action) {
        super(Type.EVENT);
        if (category != null && action != null) {
            this.data.put(FlagshipConstants.HitKeyMap.EVENT_CATEGORY, category.getName());
            this.data.put(FlagshipConstants.HitKeyMap.EVENT_ACTION, action);
        }
    }

    public Event withEventLabel(String label) {
        if (label != null)
            this.data.put(FlagshipConstants.HitKeyMap.EVENT_LABEL, label);
        return this;
    }

    public Event withEventValue(Number value) {
        if (value != null)
            this.data.put(FlagshipConstants.HitKeyMap.EVENT_VALUE, value);
        return this;
    }

    @Override
    public boolean checkData() {
        try {
            this.data.getString(FlagshipConstants.HitKeyMap.EVENT_CATEGORY);
            this.data.getString(FlagshipConstants.HitKeyMap.EVENT_ACTION);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
