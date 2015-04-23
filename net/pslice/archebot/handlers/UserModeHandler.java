package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public interface UserModeHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onUserModeSet(B bot, User user, User.Mode mode);
    void onUserModeRemoved(B bot, User user, User.Mode mode);
}
