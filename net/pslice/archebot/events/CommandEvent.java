package net.pslice.archebot.events;

import net.pslice.archebot.*;

public class CommandEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Channel channel;
    private final User user;
    private final Command<B> command;
    private final String[] args;

    public CommandEvent(B bot, Channel channel, User user, Command<B> command, String[] args) {
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

    public Command<B> getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onCommand(CommandEvent<B> event);
    }
}
