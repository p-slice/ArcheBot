package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Listener;

public interface RawListener<B extends ArcheBot> extends Listener<B> {

    public void onLine(B bot, int ID, String args);
}
