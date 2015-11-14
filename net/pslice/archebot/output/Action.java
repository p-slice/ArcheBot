package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class Action extends Output {

    public Action(Channel channel, String action, Object... objects) {
        this(channel.getName(), action, objects);
    }

    public Action(User user, String action, Object... objects) {
        this(user.getNick(), action, objects);
    }

    public Action(String target, String action, Object... objects) {
        super(String.format("PRIVMSG %s :\001ACTION %s\001", target, (objects.length > 0 ? String.format(action, objects) : action)));
    }
}
