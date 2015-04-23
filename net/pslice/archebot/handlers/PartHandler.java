package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public interface PartHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onPart(B bot, Channel channel, User user, String message);
}
