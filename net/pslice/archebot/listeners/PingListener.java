package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Listener;

public interface PingListener<B extends ArcheBot> extends Listener<B> {

    public void onPing(B bot);
}
