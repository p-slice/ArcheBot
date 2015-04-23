package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public class MessageEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Channel channel;
    private final User user;
    private final String message;

    public MessageEvent(B bot, Channel channel, User user, String message) {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.message = message;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onMessage(MessageEvent<B> event);
    }
}
