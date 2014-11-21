package net.pslice.archebot.output;

import net.pslice.archebot.*;

public final class RemoveModeMessage extends ArcheBot.Output {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public RemoveModeMessage(Channel channel, Mode.ValueMode mode)
    {
        this(channel.name, mode);
    }

    public RemoveModeMessage(String channel, Mode.ValueMode mode)
    {
        super("MODE " + channel + " -" + mode);
    }

    public RemoveModeMessage(Channel channel, Mode.PermaMode mode, String value)
    {
        this(channel.name, mode, value);
    }

    public RemoveModeMessage(String channel, Mode.PermaMode mode, String value)
    {
        super("MODE " + channel + " -" + mode + " " + value);
    }

    public RemoveModeMessage(Channel channel, User user, Mode.TempMode mode)
    {
        this(channel.name, user.getNick(), mode);
    }

    public RemoveModeMessage(String channel, String user, Mode.TempMode mode)
    {
        super("MODE " + channel + " -" + mode + " " + user);
    }

    public RemoveModeMessage(User user, User.Mode mode)
    {
        this(user.getNick(), mode);
    }

    public RemoveModeMessage(String user, User.Mode mode)
    {
        super("MODE " + user + " -" + mode);
    }
}
