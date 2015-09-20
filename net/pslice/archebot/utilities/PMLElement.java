package net.pslice.archebot.utilities;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class PMLElement implements Comparable<PMLElement> {

    private String tag, content;
    private PMLElement parent;
    private HashMap<String, PMLElement> children = new HashMap<>();
    private int indent;

    public static PMLElement read(String filename) {
        return read(filename, filename.substring(filename.lastIndexOf(File.separatorChar)).replaceAll("\\W", ""));
    }

    public static PMLElement read(String filename, String tag) {
        PMLElement element = new PMLElement(tag);

        try {
            File file = new File(filename.endsWith(".pml") ? filename : filename + ".pml");
            if (!file.exists())
                return element;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            HashMap<Integer, PMLElement> parents = new HashMap<>();
            parents.put(-1, element);
            int lastIndent = 0;

            while ((line = reader.readLine()) != null) {
                int indent = 0;

                while (line.startsWith(" ")) {
                    indent++;
                    line = line.substring(1);
                }

                int i = indent-1;
                while (!parents.containsKey(i))
                    i--;

                if (indent < lastIndent) {
                    Iterator<Integer> iterator = parents.keySet().iterator();
                    while (iterator.hasNext()) {
                        int l = iterator.next();
                        if (l > indent)
                            iterator.remove();
                    }
                }

                String[] parts = line.split(" ", 2);
                String t = parts[0];
                if (!t.matches("<#>|<[\\w.-]+>"))
                    continue;
                t = t.substring(1, t.length() - 1);
                String text = parts.length > 1 ? parts[1].replaceAll("^ +", "") : "";
                PMLElement e = new PMLElement(t, text, parents.get(i));
                e.setIndent(indent-(i < 0 ? 0 : i));
                parents.put(indent, e);

                lastIndent = indent;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading (" + e + ")");
        }

        return element;
    }

    public PMLElement(String tag) {
        this(tag, "");
    }

    public PMLElement(String tag, String content) {
        this(tag, content, null);
    }

    public PMLElement(String tag, PMLElement parent) {
        this(tag, "", parent);
    }

    public PMLElement(String tag, String content, PMLElement parent) {
        this.setTag(tag);
        this.setContent(content);
        this.setParent(parent);
        indent = 2;
    }

    public PMLElement getChild(String tag) {
        if (tag.contains("/")) {
            String[] s = tag.split("/", 2);
            return getChild(s[0]).getChild(s[1]);
        }
        if (this.isChild(tag))
            return children.get(tag.toLowerCase());
        return new PMLElement(tag, this);
    }

    public TreeSet<PMLElement> getChildren() {
        return new TreeSet<>(children.values());
    }

    public String getContent() {
        return content;
    }

    public String getFullTag() {
        return parent == null ? tag : parent.getFullTag() + "/" + tag;
    }

    public int getIndent() {
        return indent;
    }

    public PMLElement getParent() {
        return parent;
    }

    public String getTag() {
        return tag;
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean isChild(String tag) {
        if (tag.contains("/")) {
            String[] s = tag.split("/", 2);
            return isChild(s[0]) && getChild(s[0]).isChild(s[1]);
        }
        return children.containsKey(tag.toLowerCase());
    }

    public void removeChildren() {
        for (PMLElement child : this.getChildren())
            child.setParent(null);
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public void setParent(PMLElement parent) {
        if (this.parent != null) {
            this.parent.children.remove(tag.toLowerCase());
            if (tag.matches("^#\\d+$"))
                tag = "#";
        }
        if (parent != null) {
            if (tag.equals("#")) {
                int count = 0;
                for (String child : parent.children.keySet())
                    if (child.matches("^#\\d+$"))
                        count++;
                tag = "#" + count;
            }
            parent.children.put(tag.toLowerCase(), this);
        }
        this.parent = parent;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTag(String tag) {
        if (!tag.matches("#|[\\w.-]+"))
            throw new RuntimeException("Improper character(s) in tag (" + tag.replaceAll("[\\w#.-]", "") + ")");
        if (parent != null) {
            if (tag.equals("#")) {
                int count = 0;
                for (String child : parent.children.keySet())
                    if (child.matches("^#\\d+$"))
                        count++;
                tag = "#" + count;
            } else
                while (parent.isChild(tag))
                    tag += "_";
            parent.children.remove(this.tag.toLowerCase());
            parent.children.put(tag.toLowerCase(), this);
        }
        this.tag = tag;
    }

    public int size() {
        return children.size();
    }

    public void write() {
        write(tag);
    }

    public void write(String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename.endsWith(".pml") ? filename : filename + ".pml"));
            for (PMLElement child : getChildren())
                write(writer, child, -child.indent);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Error writing (" + e + ")");
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(PMLElement element) {
        if (tag.matches("#?\\d+") && element.tag.matches("#?\\d+"))
            return Integer.compare(Integer.valueOf(tag.replace("#", "")), Integer.valueOf(element.tag.replace("#", "")));
        return tag.compareToIgnoreCase(element.tag);
    }

    @Override
    public String toString() {
        return "<" + tag + "> " + content;
    }

    private static void write(BufferedWriter writer, PMLElement element, int indent) throws IOException {
        indent = indent + element.indent;
        String s = "";
        for (int i = 0; i < indent; i++)
            s += " ";
        writer.write(s);
        writer.write(element.toString().replaceAll("^<#\\d+>", "<#>"));
        writer.newLine();
        for (PMLElement child : element.getChildren())
            write(writer, child, indent);
    }
}

