package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Server;

public class MOTDEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Server server;

    public MOTDEvent(B bot, Server server) {
        super(bot);

        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onMOTD(MOTDEvent<B> event);
    }
}
