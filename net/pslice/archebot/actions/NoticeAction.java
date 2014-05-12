package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class NoticeAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final NoticeAction instance = new NoticeAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private NoticeAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static NoticeAction build(Channel channel, String notice)
    {
        return build(channel.getName(), notice);
    }

    public static NoticeAction build(User user, String notice)
    {
        return build(user.getNick(), notice);
    }

    public static NoticeAction build(String target, String notice)
    {
        instance.setText("NOTICE " + target + " :" + notice);
        return instance;
    }
}
