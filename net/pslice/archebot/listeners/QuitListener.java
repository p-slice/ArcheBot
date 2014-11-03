package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public interface QuitListener<B extends ArcheBot> extends ArcheBot.Listener<B> {

    public void onQuit(B bot, User user, String message);
}
