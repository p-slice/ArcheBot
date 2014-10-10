package net.pslice.archebot.output;

import net.pslice.archebot.IrcAction;

public final class NickMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NickMessage(String nick)
    {
        super("NICK " + nick);
    }
}
