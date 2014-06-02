package net.pslice.archebot;

public class IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // The stored message to be sent to the server
    private String text = "";

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    protected IrcAction() {}

    /*
     * =======================================
     * Protected methods:
     * =======================================
     */

    protected void setText(String text)
    {
        this.text = text;
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public final String toString()
    {
        return text;
    }

    @Override
    public final boolean equals(Object obj)
    {
        return obj instanceof IrcAction && obj.toString().equals(text);
    }
}
