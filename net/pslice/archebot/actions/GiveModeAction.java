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

    public static GiveModeAction build(Channel channel, Channel.Mode mode)
    {
        return build(channel.name, mode);
    }

    public static GiveModeAction build(String channel, Channel.Mode mode)
    {
        instance.setText("MODE " + channel + " +" + mode.toString());
        return instance;
    }

    public static GiveModeAction build(Channel channel, User user, User.Mode mode)
    {
        return build(channel.name, user.getNick(), mode);
    }

    public static GiveModeAction build(String channel, String user, User.Mode mode)
    {
        instance.setText("MODE " + channel + " " + user + " +" + mode.toString());
        return instance;
    }
}
