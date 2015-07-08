package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public class LineEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final User user;
    private final String command, tail;
    private final String[] args;

    public LineEvent(B bot, User user, String command, String[] args, String tail) {
        super(bot);

        this.user = user;
        this.command = command;
        this.args = args;
        this.tail = tail;
    }

    public User getUser() {
        return user;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public String getTail() {
        return tail;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onLine(LineEvent<B> event);
    }
}
