package net.pslice.archebot;

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
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public String getNick()
    {
        return nick;
    }

    public String getLogin()
    {
        return login;
    }

    public String getHostmask()
    {
        return hostmask;
    }

    public String getRealname()
    {
        return realname;
    }

    public String getServer()
    {
        return server;
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
