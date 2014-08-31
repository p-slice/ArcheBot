package net.pslice.archebot.actions;

import net.pslice.archebot.*;

public final class GiveModeAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public GiveModeAction(Channel channel, Mode.ValueMode mode)
    {
        this(channel.name, mode);
    }

    public GiveModeAction(String channel, Mode.ValueMode mode)
    {
        this(channel, mode, "");
    }

    public GiveModeAction(Channel channel, Mode mode, String value)
    {
        this(channel.name, mode, value);
    }

    public GiveModeAction(String channel, Mode mode, String value)
    {
        super("MODE " + channel + " +" + mode + (value.equals("") ? "" : " " + value));
    }

    public GiveModeAction(Channel channel, User user, Mode.TempMode mode)
    {
        this(channel.name, user.getNick(), mode);
    }

    public GiveModeAction(String channel, String user, Mode.TempMode mode)
    {
        super("MODE " + channel + " " + user + " +" + mode);
    }

    public GiveModeAction(User user, User.Mode mode)
    {
        this(user.getNick(), mode);
    }

    public GiveModeAction(String user, User.Mode mode)
    {
        super("MODE " + user + " +" + mode);
    }
}
