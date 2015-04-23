package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;

public interface ConnectionHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onConnect(B bot);
    void onDisconnect(B bot, String message, boolean reconnect);
}
