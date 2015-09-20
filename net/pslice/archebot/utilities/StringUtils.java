package net.pslice.archebot.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringUtils {

    private StringUtils() {}

    public static <S> String compact(S[] args) {
        return compact(args, 0);
    }

    public static <S> String compact(S[] args, int start) {
        return compact(args, start, " ");
    }

    public static <S> String compact(S[] args, String separator) {
        return compact(args, 0, separator);
    }

    public static <S> String compact(S[] args, int start, String separator) {
        String string = args[start++].toString();
        for (int i = start; i < args.length; i++)
            string += separator + args[i];
        return string;
    }

    public static <S> String compact(Collection<S> args) {
        return compact(args, ", ");
    }

    public static <S> String compact(Collection<S> args, String separator) {
        String string = "";
        for (S arg : args)
            string += separator + arg;
        return string.substring(separator.length());
    }

    public static String[] breakList(String items) {
        return items.replace(',', ' ').replaceAll(" +", " ").split(" ");
    }

    public static boolean toBoolean(String string) {
        return string.toLowerCase().equals("true");
    }

    public static String[] splitArgs(String string) {
        return splitArgs(string, '"');
    }

    public static String[] splitArgs(String string, char split) {
        List<String> strings = new ArrayList<>();
        boolean b = false;
        String current = "";
        for (char c : string.replace("\\" + split, "\000").toCharArray()) {
            if (!b && c == ' ') {
                strings.add(current.replace("\000", "" + split));
                current = "";
            } else if (c == split)
                b = !b;
            else
                current += c;
        }
        if (!current.isEmpty())
            strings.add(current.replace("\000", "" + split));
        return strings.toArray(new String[strings.size()]);
    }

    public static String[] splitArgs(String string, char first, char last) {
        if (first == last)
            return splitArgs(string, first);
        List<String> strings = new ArrayList<>();
        int i = 0;
        String current = "";
        for (char c : string.toCharArray()) {
            if (i == 0 && c == ' ') {
                strings.add(current);
                current = "";
            } else if (c == first) {
                if (i != 0)
                    current += c;
                i++;
            } else if (c == last) {
                i--;
                if (i != 0)
                    current += c;
            } else
                current += c;
        }
        if (!current.isEmpty())
            strings.add(current);
        return strings.toArray(new String[strings.size()]);
    }
}

