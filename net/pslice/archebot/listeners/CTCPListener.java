package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.Listener;
import net.pslice.archebot.User;

public interface CTCPListener<B extends ArcheBot> extends Listener<B> {

    public void onCTCPCommand(B bot, Channel channel, User sender, String command, String args);
}
