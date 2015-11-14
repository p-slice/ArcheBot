package net.pslice.archebot.output;

import net.pslice.archebot.User;

public final class ErrorMessage extends Output {

    public ErrorMessage(User user, String error, Object... objects) {
        this(user.getNick(), error, objects);
    }

    public ErrorMessage(String target, String error, Object... objects) {
        super(String.format("NOTICE %s :\002\0034Error\017: %s", target, objects.length > 0 ? String.format(error, objects) : error));
    }
}
