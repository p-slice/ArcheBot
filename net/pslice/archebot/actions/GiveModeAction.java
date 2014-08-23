package net.pslice.archebot.actions;

import net.pslice.archebot.*;

public final class GiveModeAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final GiveModeAction instance = new GiveModeAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private GiveModeAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static GiveModeAction build(Channel channel, Mode.ValueMode mode)
    {
        return build(channel.name, mode);
    }

    public static GiveModeAction build(String channel, Mode.ValueMode mode)
    {
        return build(channel, mode, "");
    }

    public static GiveModeAction build(Channel channel, Mode mode, String value)
    {
        return build(channel.name, mode, value);
    }

    public static GiveModeAction build(String channel, Mode mode, String value)
    {
        instance.setText("MODE " + channel + " +" + mode + (value.equals("") ? "" : " " + value));
        return instance;
    }

    public static GiveModeAction build(Channel channel, User user, Mode.TempMode mode)
    {
        return build(channel.name, user.getNick(), mode);
    }

    public static GiveModeAction build(String channel, String user, Mode.TempMode mode)
    {
        instance.setText("MODE " + channel + " " + user + " +" + mode);
        return instance;
    }

    public static GiveModeAction build(User user, User.Mode mode)
    {
        return build(user.getNick(), mode);
    }

    public static GiveModeAction build(String user, User.Mode mode)
    {
        instance.setText("MODE " + user + " +" + mode);
        return instance;
    }
}
