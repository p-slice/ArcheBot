package net.pslice.archebot.output;

import net.pslice.archebot.*;

public final class RemoveModeMessage extends ArcheBot.Output {

    public RemoveModeMessage(Channel channel, Mode mode) {
        this(channel.name, mode);
    }

    public RemoveModeMessage(String channel, Mode mode) {
        super("MODE " + channel + " -" + mode);
    }

    public RemoveModeMessage(Channel channel, Mode mode, String value) {
        this(channel.name, mode, value);
    }

    public RemoveModeMessage(String channel, Mode mode, String value) {
        super("MODE " + channel + " -" + mode + " " + value);
    }

    public RemoveModeMessage(Channel channel, User user, Mode mode) {
        this(channel.name, user.getNick(), mode);
    }

    public RemoveModeMessage(String channel, String user, Mode mode) {
        super("MODE " + channel + " -" + mode + " " + user);
    }
}
