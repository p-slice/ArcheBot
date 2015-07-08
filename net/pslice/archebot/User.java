package net.pslice.archebot;

import net.pslice.utilities.StringUtils;

import java.util.TreeSet;

public class User implements Comparable<User> {

    protected final TreeSet<Permission> permissions = new TreeSet<>();
    protected final TreeSet<Character> modes = new TreeSet<>();
    protected String nick, login = "", hostmask = "", realname = "", nickservID = null;
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
                nick, login, hostmask, realname, server, StringUtils.compact(permissions), StringUtils.compact(modes, ""));
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

    public TreeSet<Character> getModes() {
        return new TreeSet<>(modes);
    }

    public String getNick() {
        return nick;
    }

    public String getNickservID() {
        return nickservID;
    }

    public TreeSet<Permission> getPermissions() {
        return new TreeSet<>(permissions);
    }

    public String getRealname() {
        return realname;
    }

    public Server getServer() {
        return server;
    }

    public void give(Permission permission) {
        permissions.add(permission);
    }

    public boolean has(Permission permission) {
        for (Permission p : permissions)
            if (p.includes(permission))
                return true;
        return permissions.contains(permission);
    }

    public boolean hasMode(char mode) {
        return modes.contains(mode);
    }

    public void remove(Permission permission) {
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

    void addMode(char mode) {
        modes.add(mode);
    }

    void removeMode(char mode) {
        modes.remove(mode);
    }
}
