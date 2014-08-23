package net.pslice.archebot;

import java.util.HashMap;

public class Mode {

    /*
     * =======================================
     * Objects and variables
     * =======================================
     */

    private static final HashMap<Character, Mode> modes = new HashMap<>();

    public static final TempMode
            owner   = new TempMode('q'),
            superOp = new TempMode('a'),
            op      = new TempMode('o'),
            halfOp  = new TempMode('h'),
            voice   = new TempMode('v');

    public static final ValueMode
            noCTCP             = new ValueMode('C'),
            noColor            = new ValueMode('c'),
            opModerated        = new ValueMode('z'),
            freeTarget         = new ValueMode('F'),
            disableForward     = new ValueMode('Q'),
            permanent          = new ValueMode('P'),
            largeList          = new ValueMode('L'),
            noExternalMessages = new ValueMode('n'),
            topicProtection    = new ValueMode('t'),
            secret             = new ValueMode('s'),
            hidden             = new ValueMode('h'),
            moderated          = new ValueMode('m'),
            inviteOnly         = new ValueMode('i'),
            registeredOnly     = new ValueMode('r'),
            freeInvite         = new ValueMode('g'),
            forward            = new ValueMode('f'),
            key                = new ValueMode('k'),
            limit              = new ValueMode('l'),
            joinLimit          = new ValueMode('j');

    public static final PermaMode
            ban     = new PermaMode('b'),
            exempt  = new PermaMode('e'),
            invited = new PermaMode('I');

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
     * Public methods
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

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static class TempMode extends Mode
    {
        private TempMode(char ID)
        {
            super(ID);
        }
    }

    public static class ValueMode extends Mode
    {
        private ValueMode(char ID)
        {
            super(ID);
        }
    }

    public static class PermaMode extends Mode
    {
        private PermaMode(char ID)
        {
            super(ID);
        }
    }
}
