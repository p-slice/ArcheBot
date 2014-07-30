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

    public static GiveModeAction build(Channel channel, Mode.BasicMode mode)
    {
        return build(channel.name, mode);
    }

    public static GiveModeAction build(String channel, Mode.BasicMode mode)
    {
        instance.setText("MODE " + channel + " +" + mode);
        return instance;
    }

    public static GiveModeAction build(Channel channel, Mode.SimpleMode mode, String args)
    {
        return build(channel.name, mode, args);
    }

    public static GiveModeAction build(Channel channel, Mode.ComplexMode mode, String args)
    {
        return build(channel.name, mode, args);
    }

    private static GiveModeAction build(String channel, Mode mode, String args)
    {
        instance.setText("MODE " + channel + " +" + mode + " " + args);
        return instance;
    }

    public static GiveModeAction build(Channel channel, User user, Mode.UserMode mode)
    {
        return build(channel.name, user.getNick(), mode);
    }

    public static GiveModeAction build(String channel, String user, Mode.UserMode mode)
    {
        instance.setText("MODE " + channel + " " + user + " +" + mode);
        return instance;
    }
}
