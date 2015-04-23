package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public interface MessageHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onMessage(B bot, Channel channel, User sender, String message);
    void onPrivateMessage(B bot, User sender, String message);
}
