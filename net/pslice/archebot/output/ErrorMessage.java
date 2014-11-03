package net.pslice.archebot.output;

import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class ErrorMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ErrorMessage(User user, String error, Object... objects)
    {
        this(user.getNick(), error, objects);
    }

    public ErrorMessage(String target, String error, Object... objects)
    {
        super("NOTICE " + target + " :\u00034Error\u000F: " + String.format(error, objects));
    }
}
