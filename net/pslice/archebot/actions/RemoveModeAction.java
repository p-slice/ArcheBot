package net.pslice.archebot.actions;

import net.pslice.archebot.*;

public final class RemoveModeAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final RemoveModeAction instance = new RemoveModeAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private RemoveModeAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static RemoveModeAction build(Channel channel, Mode mode)
    {
        return build(channel.name, mode);
    }

    public static RemoveModeAction build(String channel, Mode mode)
    {
        instance.setText("MODE " + channel + " -" + mode);
        return instance;
    }

    public static RemoveModeAction build(Channel channel, Mode.ComplexMode mode, String args)
    {
        return build(channel.name, mode, args);
    }

    public static RemoveModeAction build(String channel, Mode.ComplexMode mode, String args)
    {
        instance.setText("MODE " + channel + " -" + mode + " " + args);
        return instance;
    }

    public static RemoveModeAction build(Channel channel, User user, Mode.UserMode mode)
    {
        return build(channel.name, user.getNick(), mode);
    }

    public static RemoveModeAction build(String channel, String user, Mode.UserMode mode)
    {
        instance.setText("MODE " + channel + " " + user + " -" + mode);
        return instance;
    }
}
