package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public interface CTCPHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onCTCPCommand(B bot, Channel channel, User sender, String command, String args);
}
