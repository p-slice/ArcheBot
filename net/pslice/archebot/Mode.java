package net.pslice.archebot;

import java.util.HashMap;
import java.util.HashSet;

public class Mode {

    private static final HashMap<Character, Mode> modes = new HashMap<>();
    private final char ID;

    private Mode(char ID) {
        this.ID = ID;
        modes.put(ID, this);
    }

    public char getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "" + ID;
    }

    public static Mode getMode(char ID) {
        return modes.containsKey(ID) ? modes.get(ID) : null;
    }

    public static HashSet<Mode> getModes() {
        return new HashSet<>(modes.values());
    }

    public static HashSet<Character> getModeIDs() {
        return new HashSet<>(modes.keySet());
    }

    public static boolean isMode(char ID) {
        return modes.containsKey(ID);
    }

    public static class TempMode extends Mode {

        private final char prefix;

        TempMode(char ID, char prefix) {
            super(ID);
            this.prefix = prefix;
        }

        public char getPrefix() {
            return prefix;
        }
    }

    public static class ValueMode extends Mode {
        ValueMode(char ID) {
            super(ID);
        }
    }

    public static class PermaMode extends Mode {
        PermaMode(char ID) {
            super(ID);
        }
    }
}
