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

    public static RemoveModeAction build(Channel channel, Mode.ValueMode mode)
    {
        return build(channel.name, mode);
    }

    public static RemoveModeAction build(String channel, Mode.ValueMode mode)
    {
        instance.setText("MODE " + channel + " -" + mode);
        return instance;
    }

    public static RemoveModeAction build(Channel channel, Mode.PermaMode mode, String value)
    {
        return build(channel.name, mode, value);
    }

    public static RemoveModeAction build(String channel, Mode.PermaMode mode, String value)
    {
        instance.setText("MODE " + channel + " -" + mode + " " + value);
        return instance;
    }

    public static RemoveModeAction build(Channel channel, User user, Mode.TempMode mode)
    {
        return build(channel.name, user.getNick(), mode);
    }

    public static RemoveModeAction build(String channel, String user, Mode.TempMode mode)
    {
        instance.setText("MODE " + channel + " " + user + " -" + mode);
        return instance;
    }

    public static RemoveModeAction build(User user, User.Mode mode)
    {
        return build(user.getNick(), mode);
    }

    public static RemoveModeAction build(String user, User.Mode mode)
    {
        instance.setText("MODE " + user + " -" + mode);
        return instance;
    }
}
