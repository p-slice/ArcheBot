package net.pslice.archebot.actions;

import net.pslice.archebot.IrcAction;

public final class JoinAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final JoinAction instance = new JoinAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private JoinAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static JoinAction build(String channel)
    {
        instance.setText("JOIN " + channel);
        return instance;
    }
}
