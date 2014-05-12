package net.pslice.archebot.actions;

import net.pslice.archebot.IrcAction;

public final class NickAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final NickAction instance = new NickAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private NickAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static NickAction build(String nick)
    {
        instance.setText("NICK " + nick);
        return instance;
    }
}
