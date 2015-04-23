package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;

public interface PingHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onPing(B bot);
}
