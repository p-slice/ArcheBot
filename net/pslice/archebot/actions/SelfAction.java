package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class SelfAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final SelfAction instance = new SelfAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private SelfAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static SelfAction build(Channel channel, String action)
    {
        return build(channel.name, action);
    }

    public static SelfAction build(User user, String action)
    {
        return build(user.getNick(), action);
    }

    public static SelfAction build(String target, String action)
    {
        instance.setText("PRIVMSG " + target + " :\u0001ACTION " + action + "\u0001");
        return instance;
    }
}
