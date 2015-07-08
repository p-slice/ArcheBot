package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public interface LineHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {

    void onLine(B bot, User user, String command, String[] args, String tail);
}
