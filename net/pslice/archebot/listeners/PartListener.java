package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.Listener;
import net.pslice.archebot.User;

public interface PartListener<B extends ArcheBot> extends Listener<B> {

    public void onPart(B bot, Channel channel, User user, String message);
}
