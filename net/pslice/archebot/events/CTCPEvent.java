package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public class CTCPEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Channel channel;
    private final User user;
    private final String command, args;

    public CTCPEvent(B bot, Channel channel, User user, String command, String args) {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.command = command;
        this.args = args;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public String getCommand() {
        return command;
    }

    public String getArgs() {
        return args;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onCTCPCommand(CTCPEvent<B> event);
    }
}
