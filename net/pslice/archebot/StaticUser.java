package net.pslice.archebot;

import java.util.HashSet;
import java.util.Set;

public class StaticUser {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // Info about the user
    private final String nick,
                         login,
                         hostmask,
                         realname,
                         server;

    // Set of user's modes
    protected final Set<User.Mode> modes;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    StaticUser(User user)
    {
        nick = user.getNick();
        login = user.getLogin();
        hostmask = user.getHostmask();
        realname = user.getRealname();
        server = user.getServer();
        modes = user.getModes();
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public String getHostmask()
    {
        return hostmask;
    }

    public String getLogin()
    {
        return login;
    }

    public Set<User.Mode> getModes()
    {
        return new HashSet<>(modes);
    }

    public String getNick()
    {
        return nick;
    }

    public String getRealname()
    {
        return realname;
    }

    public String getServer()
    {
        return server;
    }

    public boolean hasMode(User.Mode mode)
    {
        return modes.contains(mode);
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public String toString()
    {
        return nick + (login.equals("") ? "" : "!" + login) + (hostmask.equals("") ? "" : "@" + hostmask);
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof StaticUser && this.toString().equals(object.toString());
    }
}
