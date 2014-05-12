package net.pslice.archebot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class User {

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

    private final Set<Permission> permissions = new HashSet<>();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    User(String nick)
    {
        this.nick = nick;
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
    public boolean equals(Object obj)
    {
        return obj instanceof User && obj.toString().equals(this.toString());
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

    public static class Permission {
        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        public static final Permission DEFAULT  = new Permission("permission.default"),
                                       OPERATOR = new Permission("permission.operator");

        private static final HashMap<String, Permission> permissions = new HashMap<>();

        static
        {
            permissions.put(DEFAULT.ID, DEFAULT);
            permissions.put(OPERATOR.ID, OPERATOR);
        }

        private final String ID;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        private Permission(String ID)
        {
            this.ID = ID;
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
            Permission permission;
            if (permissions.containsKey(ID))
                permission = permissions.get(ID);
            else
            {
                permission = new Permission(ID);
                permissions.put(ID, permission);
            }

            return permission;
        }

        public static Set<Permission> getAllPermissions()
        {
            return new HashSet<>(permissions.values());
        }
    }

    public static enum Mode {
        /*
         * =======================================
         *
         * =======================================
         */

        owner('q'),
        superOp('a'),
        op('o'),
        halfOp('h'),
        voice('v'),
        banned('b'),
        invited('I');

        /*
         * =======================================
         *
         * =======================================
         */

        private final char ID;

        /*
         * =======================================
         *
         * =======================================
         */

        Mode(char ID)
        {
            this.ID = ID;
        }

        /*
         * =======================================
         *
         * =======================================
         */

        public static Mode getModeFromID(char ID)
        {
            for (Mode mode : Mode.values())
                if (mode.ID == ID)
                    return mode;
            return null;
        }

        /*
         * =======================================
         *
         * =======================================
         */

        public String toString()
        {
            return "" + ID;
        }
    }
}
