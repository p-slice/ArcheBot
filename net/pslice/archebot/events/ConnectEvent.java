package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class ConnectEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    public ConnectEvent(B bot) {
        super(bot);
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onConnect(ConnectEvent<B> event);
    }
}
