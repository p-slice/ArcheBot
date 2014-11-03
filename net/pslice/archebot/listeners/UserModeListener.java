package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public interface UserModeListener<B extends ArcheBot> extends ArcheBot.Listener<B> {

    public void onUserModeSet(B bot, User user, User.Mode mode);

    public void onUserModeRemoved(B bot, User user, User.Mode mode);
}
