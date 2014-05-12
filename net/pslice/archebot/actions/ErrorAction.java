package net.pslice.archebot.actions;

import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class ErrorAction  extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final ErrorAction instance = new ErrorAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private ErrorAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static ErrorAction build(User user, String notice)
    {
        return build(user.getNick(), notice);
    }

    public static ErrorAction build(String target, String notice)
    {
        instance.setText("NOTICE " + target + " :\u00034Error\u000F: " + notice);
        return instance;
    }
}
