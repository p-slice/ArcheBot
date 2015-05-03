package net.pslice.archebot.handlers;

import net.pslice.archebot.*;

public interface ModeHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onMode(B bot, Channel channel, User setter, Mode mode, String value, boolean added);
    void onMode(B bot, Channel channel, User setter, User receiver, Mode mode, boolean added);
}
