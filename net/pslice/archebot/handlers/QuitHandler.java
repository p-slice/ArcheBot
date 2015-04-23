package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public interface QuitHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onQuit(B bot, User user, String message);
}
