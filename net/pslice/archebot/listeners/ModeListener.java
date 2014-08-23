package net.pslice.archebot.listeners;

import net.pslice.archebot.*;

public interface ModeListener<B extends ArcheBot> extends Listener<B> {

    public void onModeSet(B bot, Channel channel, User setter, Mode mode, String value);

    public void onModeSet(B bot, Channel channel, User setter, User receiver, Mode.TempMode mode);

    public void onModeRemoved(B bot, Channel channel, User remover, Mode mode, String value);

    public void onModeRemoved(B bot, Channel channel, User remover, User receiver, Mode.TempMode mode);
}
