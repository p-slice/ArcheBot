package net.pslice.archebot;

import java.util.HashSet;
import java.util.Set;

public class User implements Comparable<User> {

    protected String nick, login = "", hostmask = "", realname = "", server = "";
    protected final Set<Permission> permissions = new HashSet<>();
    protected final Set<Mode> modes = new HashSet<>();

    protected User() {
        this("");
    }

    protected User(String nick) {
        this.nick = nick;
        permissions.add(Permission.DEFAULT);
    }

    public String details() {
        return nick + (login.isEmpty() ? "" : "!" + login) + (hostmask.isEmpty() ? "" : "@" + hostmask);
    }

    public String getHostmask() {
        return hostmask;
    }

    public String getLogin() {
        return login;
    }

    public Set<Mode> getModes() {
        return new HashSet<>(modes);
    }

    public String getNick() {
        return nick;
    }

    public Set<Permission> getPermissions() {
        return new HashSet<>(permissions);
    }

    public String getRealname() {
        return realname;
    }

    public String getServer() {
        return server;
    }

    public void givePermission(Permission permission) {
        permissions.add(permission);
    }

    public boolean hasPermission(Permission permission) {
        for (Permission p : permissions)
            if (p.includes(permission))
                return true;
        return permissions.contains(permission);
    }

    public boolean hasMode(Mode mode) {
        return modes.contains(mode);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public void resetPermissions() {
        permissions.clear();
        permissions.add(Permission.DEFAULT);
    }

    @Override
    public String toString() {
        return nick;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(User user) {
        return nick.compareToIgnoreCase(user.nick);
    }

    void addMode(Mode mode) {
        modes.add(mode);
    }

    void removeMode(Mode mode) {
        modes.remove(mode);
    }

    public static class Mode {

        private static final HashSet<Mode> modes = new HashSet<>();
        private final char ID;

        Mode(char ID) {
            this.ID = ID;
            modes.add(this);
        }

        @Override
        public String toString() {
            return "" + ID;
        }

        public static Mode getMode(char ID) {
            for (Mode mode : modes)
                if (mode.ID == ID)
                    return mode;
            return null;
        }

        public static boolean isMode(char ID) {
            for (Mode mode : modes)
                if (mode.ID == ID)
                    return true;
            return false;
        }
    }
}
