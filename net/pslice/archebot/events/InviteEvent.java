package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public class InviteEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Channel channel;
    private final User user;

    public InviteEvent(B bot, Channel channel, User user) {
        super(bot);

        this.channel = channel;
        this.user = user;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onInvite(InviteEvent<B> event);
    }
}
