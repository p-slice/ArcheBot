package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class Message extends Output {

    public Message(Channel channel, String message, Object... objects) {
        this(channel.getName(), message, objects);
    }

    public Message(User user, String message, Object... objects) {
        this(user.getNick(), message, objects);
    }

    public Message(String target, String message, Object... objects) {
        super(String.format("PRIVMSG %s :%s", target, objects.length > 0 ? String.format(message, objects) : message));
    }
}
