package net.pslice.archebot.output;

import net.pslice.archebot.IrcAction;

public final class RawMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public RawMessage(String line)
    {
        super(line);
    }
}
