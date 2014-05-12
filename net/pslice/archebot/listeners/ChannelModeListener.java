package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.Listener;
import net.pslice.archebot.User;

public interface ChannelModeListener<B extends ArcheBot> extends Listener<B> {

    public void onChannelModeSet(B bot, Channel channel, User setter, Channel.Mode mode);

    public void onChannelModeRemoved(B bot, Channel channel, User remover, Channel.Mode mode);
}
