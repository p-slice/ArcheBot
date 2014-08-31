package net.pslice.archebot;

public class IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // The message to be sent to the server
    private final String line;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    protected IrcAction(String line)
    {
        this.line = line;
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public final String toString()
    {
        return line;
    }

    @Override
    public final boolean equals(Object obj)
    {
        return obj instanceof IrcAction && obj.toString().equals(line);
    }
}
