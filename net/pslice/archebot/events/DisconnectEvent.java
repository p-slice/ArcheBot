package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class DisconnectEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final String message;

    public DisconnectEvent(B bot, String message) {
        super(bot);

        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onDisconnect(DisconnectEvent<B> event);
    }
}
