package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public interface NickChangeListener<B extends ArcheBot> extends ArcheBot.Listener<B> {

    public void onNickChange(B bot, User user, String oldNick);
}
