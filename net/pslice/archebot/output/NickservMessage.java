package net.pslice.archebot.output;

import net.pslice.archebot.IrcAction;

public final class NickservMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NickservMessage(String nick, String password)
    {
        this(nick + " " + password);
    }

    public NickservMessage(String password)
    {
        super("NICKSERV IDENTIFY " + password);
    }
}
