package net.pslice.archebot.actions;

import net.pslice.archebot.IrcAction;

public final class NickservAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NickservAction(String nick, String password)
    {
        this(nick + " " + password);
    }

    public NickservAction(String password)
    {
        super("NICKSERV IDENTIFY " + password);
    }
}
