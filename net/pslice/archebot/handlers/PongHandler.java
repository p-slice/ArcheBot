package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;

public interface PongHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onPong(B bot, String server, String message);
}
