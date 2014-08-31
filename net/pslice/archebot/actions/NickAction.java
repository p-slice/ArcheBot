package net.pslice.archebot.actions;

import net.pslice.archebot.IrcAction;

public final class NickAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NickAction(String nick)
    {
        super("NICK " + nick);
    }
}
