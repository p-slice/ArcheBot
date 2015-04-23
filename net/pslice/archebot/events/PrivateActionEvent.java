package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public class PrivateActionEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final User user;
    private final String action;

    public PrivateActionEvent(B bot, User user, String action) {
        super(bot);

        this.user = user;
        this.action = action;
    }

    public User getUser() {
        return user;
    }

    public String getAction() {
        return action;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onPrivateAction(PrivateActionEvent<B> event);
    }
}
