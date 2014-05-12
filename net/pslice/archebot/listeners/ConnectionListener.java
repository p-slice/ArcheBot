package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Listener;

public interface ConnectionListener<B extends ArcheBot> extends Listener<B> {

    public void onConnect(B bot);

    public void onDisconnect(B bot, String message, boolean reconnect);
}
