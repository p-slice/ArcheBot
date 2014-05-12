package net.pslice.archebot.actions;

import net.pslice.archebot.IrcAction;

public final class RawAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final RawAction instance = new RawAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private RawAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static RawAction build(String line)
    {
        instance.setText(line);
        return instance;
    }
}
