package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.Listener;
import net.pslice.archebot.User;

public interface InviteListener<B extends ArcheBot> extends Listener<B> {

    public void onInvite(B bot, Channel channel, User user);
}