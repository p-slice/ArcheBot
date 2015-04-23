package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public interface ActionHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onAction(B bot, Channel channel, User sender, String action);
    void onPrivateAction(B bot, User sender, String action);
}
