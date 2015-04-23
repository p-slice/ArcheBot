package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public class NickEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final User user;
    private final String oldNick;

    public NickEvent(B bot, User user, String oldNick) {
        super(bot);

        this.user = user;
        this.oldNick = oldNick;
    }

    public User getUser() {
        return user;
    }

    public String getOldNick() {
        return oldNick;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onNickChange(NickEvent<B> event);
    }
}
