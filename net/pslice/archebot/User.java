package net.pslice.archebot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class User implements Comparable<User> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // Information about the user
    protected String nick     = "",
                     login    = "",
                     hostmask = "",
                     realname = "",
                     server   = "";

    // Set of user's permissions
    protected final Set<Permission> permissions = new HashSet<>();

    // Set of user's modes
    protected final Set<Mode> modes = new HashSet<>();

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

    public String getHostmask()
    {
        return hostmask;
    }

    public String getLogin()
    {
        return login;
    }

    public Set<Mode> getModes()
    {
        return new HashSet<>(modes);
    }

    public String getNick()
    {
        return nick;
    }

    public Set<Permission> getPermissions()
    {
        return new HashSet<>(permissions);
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

    public boolean hasPermission(Permission permission)
    {
        return permissions.contains(permission);
    }

    public boolean hasMode(Mode mode)
    {
        return modes.contains(mode);
    }

    public void removePermission(Permission permission)
    {
        permissions.remove(permission);
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

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(User user)
    {
        return nick.toLowerCase().compareTo(user.nick.toLowerCase());
    }

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    void addMode(Mode mode)
    {
        modes.add(mode);
    }

    void removeMode(Mode mode)
    {
        modes.remove(mode);
    }

    void setHostmask(String hostmask)
    {
        this.hostmask = hostmask;
    }

    void setLogin(String login)
    {
        this.login = login;
    }

    void setNick(String nick)
    {
        this.nick = nick;
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

    public static enum Mode
    {
        /*
         * =======================================
         * Enum values:
         * =======================================
         */

        away('a'),
        deaf('D'),
        invisible('i'),
        operator('o'),
        viewWallops('w'),
        ssl('Z');

        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        private final char ID;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        private Mode(char ID)
        {
            this.ID = ID;
        }

        /*
         * =======================================
         * Overridden methods:
         * =======================================
         */

        @Override
        public String toString()
        {
            return "" + ID;
        }

        /*
         * =======================================
         * Static methods:
         * =======================================
         */

        public static Mode getMode(char ID)
        {
            for (Mode mode : Mode.values())
                if (mode.ID == ID)
                    return mode;
            return null;
        }
    }
}
