package net.pslice.archebot.handlers;

import net.pslice.archebot.*;

public interface ModeHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onModeSet(B bot, Channel channel, User setter, Mode mode, String value);
    void onModeSet(B bot, Channel channel, User setter, User receiver, Mode.TempMode mode);
    void onModeRemoved(B bot, Channel channel, User remover, Mode mode, String value);
    void onModeRemoved(B bot, Channel channel, User remover, User receiver, Mode.TempMode mode);
}
