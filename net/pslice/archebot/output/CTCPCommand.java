package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class CTCPCommand extends Output {

    public CTCPCommand(Channel channel, String command, String args, Object... objects) {
        this(channel.getName(), command, args, objects);
    }

    public CTCPCommand(User user, String command, String args, Object... objects) {
        this(user.getNick(), command, args, objects);
    }

    public CTCPCommand(String target, String command, String args, Object... objects) {
        super(String.format("PRIVMSG %s :\001%s %s\001", target, command.toUpperCase(), objects.length > 0 ? String.format(args, objects) : args));
    }
}
