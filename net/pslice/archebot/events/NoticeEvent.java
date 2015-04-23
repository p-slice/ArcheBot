package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public class NoticeEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Channel channel;
    private final User user;
    private final String notice;

    public NoticeEvent(B bot, Channel channel, User user, String notice) {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.notice = notice;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public String getNotice() {
        return notice;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onNotice(NoticeEvent<B> event);
    }
}
