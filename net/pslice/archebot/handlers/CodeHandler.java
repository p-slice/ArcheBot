package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;

public interface CodeHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onCode(B bot, int ID, String[] args, String tail);
}
