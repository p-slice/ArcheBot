package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class PingEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    public PingEvent(B bot) {
        super(bot);
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onPing(PingEvent<B> event);
    }
}
