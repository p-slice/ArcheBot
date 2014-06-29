package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class InviteAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final InviteAction instance = new InviteAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private InviteAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static InviteAction build(Channel channel, User user)
    {
        return build(channel.name, user.getNick());
    }

    public static InviteAction build(String channel, String user)
    {
        instance.setText("INVITE " + user + " " + channel);
        return instance;
    }
}
