package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class AddModeMessage extends Output {

    public AddModeMessage(Channel channel, char mode) {
        this(channel.getName(), mode);
    }

    public AddModeMessage(String channel, char mode) {
        this(channel, mode, "");
    }

    public AddModeMessage(Channel channel, char mode, String value) {
        this(channel.getName(), mode, value);
    }

    public AddModeMessage(Channel channel, char mode, User user) {
        this(channel.getName(), mode, user.getNick());
    }

    public AddModeMessage(String channel, char mode, String value) {
        super(String.format("MODE %s +%s %s", channel, mode, value));
    }
}
