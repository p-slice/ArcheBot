package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class MessageAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final MessageAction instance = new MessageAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private MessageAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static MessageAction build(Channel channel, String message)
    {
        return build(channel.getName(), message);
    }

    public static MessageAction build(User user, String message)
    {
        return build(user.getNick(), message);
    }

    public static MessageAction build(String target, String message)
    {
        instance.setText("PRIVMSG " + target + " :" + message);
        return instance;
    }
}
