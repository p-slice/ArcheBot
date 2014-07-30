package net.pslice.archebot;

import java.util.HashMap;

public class Mode {

    /*
     * =======================================
     * Objects and variables
     * =======================================
     */

    private static final HashMap<Character, Mode> modes = new HashMap<>();

    public static final UserMode
        owner   = new UserMode('q'),
        superOp = new UserMode('a'),
        op      = new UserMode('o'),
        halfOp  = new UserMode('h'),
        voice   = new UserMode('v');

    public static final BasicMode
        noCTCP             = new BasicMode('C'),
        noColor            = new BasicMode('c'),
        opModerated        = new BasicMode('z'),
        freeTarget         = new BasicMode('F'),
        disableForward     = new BasicMode('Q'),
        permanent          = new BasicMode('P'),
        largeList          = new BasicMode('L'),
        noExternalMessages = new BasicMode('n'),
        topicProtection    = new BasicMode('t'),
        secret             = new BasicMode('s'),
        hidden             = new BasicMode('h'),
        moderated          = new BasicMode('m'),
        inviteOnly         = new BasicMode('i'),
        registeredOnly     = new BasicMode('r'),
        freeInvite         = new BasicMode('g');

    public static final SimpleMode
        forward   = new SimpleMode('f'),
        key       = new SimpleMode('k'),
        limit     = new SimpleMode('l'),
        joinLimit = new SimpleMode('j');

    public static final ComplexMode
        ban = new ComplexMode('b'),
        exempt = new ComplexMode('e'),
        invited = new ComplexMode('I');

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

    public static class UserMode extends Mode
    {
        private UserMode(char ID)
        {
            super(ID);
        }
    }

    public static class BasicMode extends Mode
    {
        private BasicMode(char ID)
        {
            super(ID);
        }
    }

    public static class SimpleMode extends Mode
    {
        private SimpleMode(char ID)
        {
            super(ID);
        }
    }

    public static class ComplexMode extends Mode
    {
        private ComplexMode(char ID)
        {
            super(ID);
        }
    }
}
