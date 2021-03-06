package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class KickMessage extends Output {

    public KickMessage(Channel channel, User user) {
        this(channel.getName(), user.getNick());
    }

    public KickMessage(String channel, String user) {
        this(channel, user, "");
    }

    public KickMessage(Channel channel, User user, String reason, Object... objects) {
        this(channel.getName(), user.getNick(), reason, objects);
    }

    public KickMessage(String channel, String user, String reason, Object... objects) {
        super(String.format("KICK %s %s :%s", channel, user, objects.length > 0 ? String.format(reason, objects) : reason));
    }
}
