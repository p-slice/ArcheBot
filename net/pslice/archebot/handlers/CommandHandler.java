package net.pslice.archebot.handlers;

import net.pslice.archebot.*;

public interface CommandHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onCommand(B bot, Channel channel, User sender, Command<B> command, String[] args);
}
