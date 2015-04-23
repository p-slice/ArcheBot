package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public interface KickHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onKick(B bot, Channel channel, User kicker, User receiver, String reason);
}
