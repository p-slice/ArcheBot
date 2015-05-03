package net.pslice.archebot.output;

import net.pslice.archebot.*;

public final class AddModeMessage extends ArcheBot.Output {

    public AddModeMessage(Channel channel, Mode mode) {
        this(channel.name, mode);
    }

    public AddModeMessage(String channel, Mode mode) {
        this(channel, mode, "");
    }

    public AddModeMessage(Channel channel, Mode mode, String value) {
        this(channel.name, mode, value);
    }

    public AddModeMessage(String channel, Mode mode, String value) {
        super("MODE " + channel + " +" + mode + (value.isEmpty() ? "" : " " + value));
    }

    public AddModeMessage(Channel channel, User user, Mode mode) {
        this(channel.name, user.getNick(), mode);
    }

    public AddModeMessage(String channel, String user, Mode mode) {
        super("MODE " + channel + " +" + mode + " " + user);
    }
}
