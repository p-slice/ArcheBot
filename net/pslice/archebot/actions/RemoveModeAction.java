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

    public static RemoveModeAction build(Channel channel, Channel.Mode mode)
    {
        return build(channel.getName(), mode);
    }

    public static RemoveModeAction build(String channel, Channel.Mode mode)
    {
        instance.setText("MODE " + channel + " -" + mode.toString());
        return instance;
    }

    public static RemoveModeAction build(Channel channel, User user, User.Mode mode)
    {
        return build(channel.getName(), user.getNick(), mode);
    }

    public static RemoveModeAction build(String channel, String user, User.Mode mode)
    {
        instance.setText("MODE " + channel + " " + user + " -" + mode.toString());
        return instance;
    }
}
