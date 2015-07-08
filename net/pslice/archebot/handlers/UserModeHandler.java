package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public interface UserModeHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onUserMode(B bot, User user, char mode, boolean added);
}
