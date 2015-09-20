package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class RemoveModeMessage extends Output {

    public RemoveModeMessage(Channel channel, char mode) {
        this(channel.getName(), mode);
    }

    public RemoveModeMessage(String channel, char mode) {
        super("MODE " + channel + " -" + mode);
    }

    public RemoveModeMessage(Channel channel, char mode, String value) {
        this(channel.getName(), mode, value);
    }

    public RemoveModeMessage(String channel, char mode, String value) {
        super("MODE " + channel + " -" + mode + " " + value);
    }

    public RemoveModeMessage(Channel channel, User user, char mode) {
        this(channel.getName(), user.getNick(), mode);
    }

    public RemoveModeMessage(String channel, String user, char mode) {
        super("MODE " + channel + " -" + mode + " " + user);
    }
}
