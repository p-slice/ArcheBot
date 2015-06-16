package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;

public interface RawHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onLine(B bot, int ID, String[] args, String tail);
}
