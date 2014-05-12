package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.Listener;
import net.pslice.archebot.User;

public interface KickListener<B extends ArcheBot> extends Listener<B> {

    public void onKick(B bot, Channel channel, User kicker, User receiver, String reason);
}
