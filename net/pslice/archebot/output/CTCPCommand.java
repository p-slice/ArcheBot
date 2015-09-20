package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class CTCPCommand extends Output {

    public CTCPCommand(Channel channel, String command) {
        this(channel.getName(), command);
    }

    public CTCPCommand(User user, String command) {
        this(user.getNick(), command);
    }

    public CTCPCommand(String target, String command) {
        this(target, command, "");
    }

    public CTCPCommand(Channel channel, String command, String message, Object... objects) {
        this(channel.getName(), command, message, objects);
    }

    public CTCPCommand(User user, String command, String message, Object... objects) {
        this(user.getNick(), command, message, objects);
    }

    public CTCPCommand(String target, String command, String message, Object... objects) {
        super("PRIVMSG " + target + " :\001" + command.toUpperCase() + (message.isEmpty() ? "" : " " + String.format(message, objects)) + "\001");
    }
}
