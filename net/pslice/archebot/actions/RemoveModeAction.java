package net.pslice.archebot.actions;

import net.pslice.archebot.*;

public final class RemoveModeAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public RemoveModeAction(Channel channel, Mode.ValueMode mode)
    {
        this(channel.name, mode);
    }

    public RemoveModeAction(String channel, Mode.ValueMode mode)
    {
        super("MODE " + channel + " -" + mode);
    }

    public RemoveModeAction(Channel channel, Mode.PermaMode mode, String value)
    {
        this(channel.name, mode, value);
    }

    public RemoveModeAction(String channel, Mode.PermaMode mode, String value)
    {
        super("MODE " + channel + " -" + mode + " " + value);
    }

    public RemoveModeAction(Channel channel, User user, Mode.TempMode mode)
    {
        this(channel.name, user.getNick(), mode);
    }

    public RemoveModeAction(String channel, String user, Mode.TempMode mode)
    {
        super("MODE " + channel + " -" + mode + " " + user);
    }

    public RemoveModeAction(User user, User.Mode mode)
    {
        this(user.getNick(), mode);
    }

    public RemoveModeAction(String user, User.Mode mode)
    {
        super("MODE " + user + " -" + mode);
    }
}
