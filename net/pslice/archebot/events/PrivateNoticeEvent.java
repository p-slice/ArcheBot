package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public class PrivateNoticeEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final User user;
    private final String notice;

    public PrivateNoticeEvent(B bot, User user, String notice) {
        super(bot);

        this.user = user;
        this.notice = notice;
    }

    public User getUser() {
        return user;
    }

    public String getNotice() {
        return notice;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onPrivateNotice(PrivateNoticeEvent<B> event);
    }
}
