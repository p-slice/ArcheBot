package net.pslice.archebot;

import java.util.HashMap;
import java.util.HashSet;

public class Mode {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // All known modes
    private static final HashMap<Character, Mode> modes = new HashMap<>();
    // The ID of the mode
    private final char ID;

    /*
     * =======================================
     * Constructors
     * =======================================
     */

    private Mode(char ID)
    {
        this.ID = ID;
        modes.put(ID, this);
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public char getID()
    {
        return ID;
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public String toString()
    {
        return "" + ID;
    }

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static Mode getMode(char ID)
    {
        return modes.containsKey(ID) ? modes.get(ID) : null;
    }

    public static HashSet<Mode> getModes()
    {
        return new HashSet<>(modes.values());
    }

    public static HashSet<Character> getModeIDs()
    {
        return new HashSet<>(modes.keySet());
    }

    public static boolean isMode(char ID)
    {
        return modes.containsKey(ID);
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static class TempMode extends Mode
    {
        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        // User name prefix
        private final char prefix;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        TempMode(char ID, char prefix)
        {
            super(ID);
            this.prefix = prefix;
        }

        /*
         * =======================================
         * Public methods:
         * =======================================
         */

        public char getPrefix()
        {
            return prefix;
        }
    }

    public static class ValueMode extends Mode
    {
        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        ValueMode(char ID)
        {
            super(ID);
        }
    }

    public static class PermaMode extends Mode
    {
        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        PermaMode(char ID)
        {
            super(ID);
        }
    }
}
