package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class InviteMessage extends Output {

    public InviteMessage(Channel channel, User user) {
        this(channel.getName(), user.getNick());
    }

    public InviteMessage(String channel, String user) {
        super(String.format("INVITE %s %s", user, channel));
    }
}
