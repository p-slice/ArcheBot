package net.pslice.archebot.output;

import net.pslice.archebot.*;

public final class AddModeMessage extends ArcheBot.Output {

    public AddModeMessage(Channel channel, Mode.ValueMode mode) {
        this(channel.name, mode);
    }

    public AddModeMessage(String channel, Mode.ValueMode mode) {
        this(channel, mode, "");
    }

    public AddModeMessage(Channel channel, Mode mode, String value) {
        this(channel.name, mode, value);
    }

    public AddModeMessage(String channel, Mode mode, String value) {
        super("MODE " + channel + " +" + mode + (value.isEmpty() ? "" : " " + value));
    }

    public AddModeMessage(Channel channel, User user, Mode.TempMode mode) {
        this(channel.name, user.getNick(), mode);
    }

    public AddModeMessage(String channel, String user, Mode.TempMode mode) {
        super("MODE " + channel + " +" + mode + " " + user);
    }

    public AddModeMessage(User user, User.Mode mode) {
        this(user.getNick(), mode);
    }

    public AddModeMessage(String user, User.Mode mode) {
        super("MODE " + user + " +" + mode);
    }
}
