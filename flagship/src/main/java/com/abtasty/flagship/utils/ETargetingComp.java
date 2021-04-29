package com.abtasty.flagship.utils;

import org.json.JSONArray;

public enum ETargetingComp implements ITargetingComp {

    EQUALS("EQUALS") {

        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return contextValue == flagshipValue;
        }

        @Override
        public boolean compareNumbers(Number contextValue, Number flagshipValue) {
            return contextValue.doubleValue() == flagshipValue.doubleValue();
        }

        @Override
        public boolean compareInJsonArray(Object contextValue, JSONArray flagshipValue) {
            for (int i = 0; i < flagshipValue.length(); i++) {
                Object obj = flagshipValue.get(i);
                if ((contextValue instanceof Number && obj instanceof Number) && (compareNumbers((Number) contextValue, (Number) obj)))
                    return true;
                else if (compareObjects(contextValue, obj))
                    return true;
            }
            return false;
        }
    },

    NOT_EQUALS("NOT_EQUALS") {

        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return contextValue != flagshipValue;
        }

        @Override
        public boolean compareNumbers(Number contextValue, Number flagshipValue) {
            return contextValue.doubleValue() != flagshipValue.doubleValue();
        }

        @Override
        public boolean compareInJsonArray(Object contextValue, JSONArray flagshipValue) {
            for (int i = 0; i < flagshipValue.length(); i++) {
                Object obj = flagshipValue.get(i);
                if ((contextValue instanceof Number && obj instanceof Number) && !(compareNumbers((Number) contextValue, (Number) obj)))
                    return false;
                else if (!compareObjects(contextValue, obj))
                    return false;
            }
            return true;
        }
    },

    CONTAINS("CONTAINS") {

        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return contextValue.toString().contains(flagshipValue.toString());
        }

        @Override
        public boolean compareInJsonArray(Object contextValue, JSONArray flagshipValue) {
            for (int i = 0; i < flagshipValue.length(); i++) {
                Object obj = flagshipValue.get(i);
                if (compareObjects(contextValue, obj))
                    return true;
            }
            return false;
        }
    },

    NOT_CONTAINS("NOT_CONTAINS") {
        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return !contextValue.toString().contains(flagshipValue.toString());
        }

        @Override
        public boolean compareInJsonArray(Object contextValue, JSONArray flagshipValue) {
            for (int i = 0; i < flagshipValue.length(); i++) {
                Object obj = flagshipValue.get(i);
                if (!compareObjects(contextValue, obj))
                    return false;
            }
            return true;
        }
    },

    GREATER_THAN("GREATER_THAN") {
        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return contextValue.toString().compareTo(flagshipValue.toString()) > 0;
        }

        @Override
        public boolean compareNumbers(Number contextValue, Number flagshipValue) {
            return contextValue.doubleValue() > flagshipValue.doubleValue();
        }
    },

    LOWER_THAN("LOWER_THAN") {
        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return contextValue.toString().compareTo(flagshipValue.toString()) < 0;
        }

        @Override
        public boolean compareNumbers(Number contextValue, Number flagshipValue) {
            return contextValue.doubleValue() < flagshipValue.doubleValue();
        }
    },

    GREATER_THAN_OR_EQUALS("GREATER_THAN_OR_EQUALS") {
        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return contextValue.toString().compareTo(flagshipValue.toString()) >= 0;
        }

        @Override
        public boolean compareNumbers(Number contextValue, Number flagshipValue) {
            return contextValue.doubleValue() >= flagshipValue.doubleValue();
        }
    },

    LOWER_THAN_OR_EQUALS("LOWER_THAN_OR_EQUALS") {
        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return contextValue.toString().compareTo(flagshipValue.toString()) <= 0;
        }

        @Override
        public boolean compareNumbers(Number contextValue, Number flagshipValue) {
            return contextValue.doubleValue() <= flagshipValue.doubleValue();
        }
    },

    STARTS_WITH("STARTS_WITH") {
        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return contextValue.toString().startsWith(flagshipValue.toString());
        }
    },

    ENDS_WITH("ENDS_WITH") {
        @Override
        public boolean compareObjects(Object contextValue, Object flagshipValue) {
            return contextValue.toString().endsWith(flagshipValue.toString());
        }
    },

    ;

    String name;

    ETargetingComp(String name) {
        this.name = name;
    }

    public static ETargetingComp get(String name) {
        for (ETargetingComp e : ETargetingComp.values()) {
            if (e.name.equals(name))
                return e;
        }
        return null;
    }
}
