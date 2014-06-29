package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;

public final class PartAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final PartAction instance = new PartAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private PartAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static PartAction build(Channel channel)
    {
        return build(channel.toString());
    }

    public static PartAction build(Channel channel, String reason)
    {
        return build(channel.name, reason);
    }

    public static PartAction build(String channel)
    {
        instance.setText("Part " + channel);
        return instance;
    }

    public static PartAction build(String channel, String reason)
    {
        instance.setText("PART " + channel + " :" + reason);
        return instance;
    }
}
