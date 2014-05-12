package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class KickAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables
     * =======================================
     */

    private static final KickAction instance = new KickAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private KickAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static KickAction build(Channel channel, User user)
    {
        return build(channel.toString(), user.toString());
    }

    public static KickAction build(Channel channel, User user, String reason)
    {
        return build(channel.getName(), user.getNick(), reason);
    }

    public static KickAction build(String channel, String user)
    {
        instance.setText("KICK " + channel + " " + user);
        return instance;
    }

    public static KickAction build(String channel, String user, String reason)
    {
        instance.setText("KICK " + channel + " " + user +  " :" + reason);
        return instance;
    }
}
