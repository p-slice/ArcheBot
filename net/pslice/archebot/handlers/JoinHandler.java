package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public interface JoinHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onJoin(B bot, Channel channel, User user);
}