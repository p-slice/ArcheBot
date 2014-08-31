package net.pslice.archebot.actions;

import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class ErrorAction  extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ErrorAction(User user, String error)
    {
        this(user.getNick(), error);
    }

    public ErrorAction(String target, String error)
    {
        super("NOTICE " + target + " :\u00034Error\u000F: " + error);
    }
}
