package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class Notice extends Output {

    public Notice(Channel channel, String notice, Object... objects) {
        this(channel.getName(), notice, objects);
    }

    public Notice(User user, String notice, Object... objects) {
        this(user.getNick(), notice, objects);
    }

    public Notice(String target, String notice, Object... objects) {
        super(String.format("NOTICE %s :%s", target, objects.length > 0 ? String.format(notice, objects) : notice));
    }
}
