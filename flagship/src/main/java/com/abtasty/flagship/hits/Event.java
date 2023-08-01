package com.abtasty.flagship.hits;

import com.abtasty.flagship.utils.FlagshipConstants;

public class Event extends Hit<Event> {

    public enum EventCategory {

        ACTION_TRACKING("Action Tracking"),
        USER_ENGAGEMENT("User Engagement");

        private String name;

        EventCategory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Hit which represents an event. Can be anything you want :  for example a click or a newsletter subscription.
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

    /**
     * Specifies a label for this event (optional)
     *
     * @param label label of the event
     */
    public Event withEventLabel(String label) {
        if (label != null)
            this.data.put(FlagshipConstants.HitKeyMap.EVENT_LABEL, label);
        return this;
    }

    /**
     * Specifies a value for this event. must be non-negative integer superior to 0. (optional)
     *
     * @param value value of the event
     */
    public Event withEventValue(int value) {
        if (value > 0)
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
