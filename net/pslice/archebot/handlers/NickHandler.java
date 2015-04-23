package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public interface NickHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onNickChange(B bot, User user, String oldNick);
}
