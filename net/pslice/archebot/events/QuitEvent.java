package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public class QuitEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final User user;
    private final String message;

    public QuitEvent(B bot, User user, String message) {
        super(bot);

        this.user = user;
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onQuit(QuitEvent<B> event);
    }
}
