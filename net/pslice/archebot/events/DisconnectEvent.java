package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class DisconnectEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final String message;
    private final boolean reconnect;

    public DisconnectEvent(B bot, String message, boolean reconnect) {
        super(bot);

        this.message = message;
        this.reconnect = reconnect;
    }

    public String getMessage() {
        return message;
    }

    public boolean getReconnect() {
        return reconnect;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onDisconnect(DisconnectEvent<B> event);
    }
}
