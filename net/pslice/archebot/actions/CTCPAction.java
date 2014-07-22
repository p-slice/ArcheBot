package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;

public final class CTCPAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final CTCPAction instance = new CTCPAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private CTCPAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static CTCPAction build(Channel channel, String command)
    {
        return build(channel.name, command);
    }

    public static CTCPAction build(String channel, String command)
    {
        instance.setText("PRIVMSG " + channel + " :\u0001" + command.toUpperCase() + "\u0001");
        return instance;
    }

    public static CTCPAction build(Channel channel, String command, String message)
    {
        return build(channel.name, command, message);
    }

    public static CTCPAction build(String channel, String command, String message)
    {
        instance.setText("PRIVMSG " + channel + " :\u0001" + command.toUpperCase() + " " + message + "\u0001");
        return instance;
    }
}
