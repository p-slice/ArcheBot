package net.pslice.archebot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class User {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // Information about the user
    private String nick     = "",
                   login    = "",
                   hostmask = "",
                   realname = "",
                   server   = "";

    // Set of user's permissions
    private final Set<Permission> permissions = new HashSet<>();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    User(String nick)
    {
        this.nick = nick;
        permissions.add(Permission.DEFAULT);
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

    public void givePermission(Permission permission)
    {
        permissions.add(permission);
    }

    public void removePermission(Permission permission)
    {
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission)
    {
        return permissions.contains(permission);
    }

    public Set<Permission> getPermissions()
    {
        return new HashSet<>(permissions);
    }

    public void resetPermissions()
    {
        permissions.clear();
        permissions.add(Permission.DEFAULT);
    }

    public StaticUser toStaticUser()
    {
        return new StaticUser(this);
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

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    void setNick(String nick)
    {
        this.nick = nick;
    }

    void setLogin(String login)
    {
        this.login = login;
    }

    void setHostmask(String hostmask)
    {
        this.hostmask = hostmask;
    }

    void setRealname(String realname)
    {
        this.realname = realname;
    }

    void setServer(String server)
    {
        this.server = server;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static class Permission
    {
        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        private static final HashMap<String, Permission> permissions = new HashMap<>();

        public static final Permission DEFAULT  = new Permission("permission.default"),
                                       OPERATOR = new Permission("permission.operator");

        private final String ID;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        private Permission(String ID)
        {
            this.ID = ID;
            permissions.put(ID, this);
        }

        /*
         * =======================================
         * Overridden methods:
         * =======================================
         */

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof Permission && obj.toString().equals(this.toString());
        }

        @Override
        public String toString()
        {
            return ID;
        }

        /*
         * =======================================
         * Static methods:
         * =======================================
         */

        public static boolean isPermission(String ID)
        {
            return permissions.containsKey(ID);
        }

        public static Permission generate(String ID)
        {
            return permissions.containsKey(ID) ? permissions.get(ID) : new Permission(ID);
        }

        public static Set<Permission> getAllPermissions()
        {
            return new HashSet<>(permissions.values());
        }
    }
}
