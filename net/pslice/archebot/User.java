package net.pslice.archebot;

import net.pslice.utilities.StringUtils;

import java.util.HashSet;

public class User implements Comparable<User> {

    protected final HashSet<Permission> permissions = new HashSet<>();
    protected final HashSet<Mode> modes = new HashSet<>();
    protected String nick, login = "", hostmask = "", realname = "";
    protected Server server;

    protected User() {
        this("");
    }

    protected User(String nick) {
        this.nick = nick;
        permissions.add(Permission.DEFAULT);
    }

    public String details() {
        return String.format("%s {LOGIN:%s} {HOSTMASK:%s} {REALNAME:%s} {SERVER:%s} {PERMISSIONS:%s} {MODES:%s}",
                nick, login, hostmask, realname, server, StringUtils.compact(permissions, ", "), StringUtils.compact(modes, ""));
    }

    public String getIdentity() {
        return nick + (login.isEmpty() ? "" : "!" + login) + (hostmask.isEmpty() ? "" : "@" + hostmask);
    }

    public String getHostmask() {
        return hostmask;
    }

    public String getLogin() {
        return login;
    }

    public HashSet<Mode> getModes() {
        return new HashSet<>(modes);
    }

    public String getNick() {
        return nick;
    }

    public HashSet<Permission> getPermissions() {
        return new HashSet<>(permissions);
    }

    public String getRealname() {
        return realname;
    }

    public Server getServer() {
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

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(User user) {
        return nick.compareToIgnoreCase(user.nick);
    }

    @Override
    public String toString() {
        return nick;
    }

    void addMode(Mode mode) {
        modes.add(mode);
    }

    void removeMode(Mode mode) {
        modes.remove(mode);
    }
}
