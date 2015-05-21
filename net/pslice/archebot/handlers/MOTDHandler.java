package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Server;

public interface MOTDHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onMOTD(B bot, Server server);
}
