package net.pslice.archebot.actions;

import net.pslice.archebot.IrcAction;

public final class NickservAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final NickservAction instance = new NickservAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private NickservAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static NickservAction build(String nick, String password)
    {
        return build(nick + " " + password);
    }

    public static NickservAction build(String password)
    {
        instance.setText("NICKSERV IDENTIFY " + password);
        return instance;
    }
}
