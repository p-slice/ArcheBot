package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Mode;
import net.pslice.archebot.User;

public class UserModeEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final User user;
    private final Mode mode;
    private final boolean added;

    public UserModeEvent(B bot, User user, Mode mode, boolean added) {
        super(bot);

        this.user = user;
        this.mode = mode;
        this.added = added;
    }

    public User getUser() {
        return user;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean modeAdded() {
        return added;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onUserMode(UserModeEvent<B> event);
    }
}
