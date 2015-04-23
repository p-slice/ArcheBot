package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public class KickEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Channel channel;
    private final User kicker, receiver;
    private final String message;

    public KickEvent(B bot, Channel channel, User kicker, User receiver, String message) {
        super(bot);

        this.channel = channel;
        this.kicker = kicker;
        this.receiver = receiver;
        this.message = message;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getKicker() {
        return kicker;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onKick(KickEvent<B> event);
    }
}
