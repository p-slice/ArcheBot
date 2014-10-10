package net.pslice.archebot.output;

import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class ErrorMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ErrorMessage(User user, String error)
    {
        this(user.getNick(), error);
    }

    public ErrorMessage(String target, String error)
    {
        super("NOTICE " + target + " :\u00034Error\u000F: " + error);
    }
}
