package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class PongEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final String server, message;

    public PongEvent(B bot, String server, String message) {
        super(bot);

        this.server = server;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getServer() {
        return server;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onPong(PongEvent<B> event);
    }
}
