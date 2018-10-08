package org.apache.fulcrum.pool;

import java.lang.reflect.Method;

/**
 * An inner class for cached recycle methods.
 */
public class Recycler
{
    /**
     * The method.
     */
    private final Method recycle;
    /**
     * The signature.
     */
    private final String[] signature;
    /**
     * Constructs a new recycler.
     *
     * @param rec the recycle method.
     * @param sign the signature.
     */
    public Recycler(Method rec, String[] sign)
    {
        recycle = rec;
        signature = (sign != null) && (sign.length > 0) ? sign : null;
    }
    /**
     * Matches the given signature against
     * that of the recycle method of this recycler.
     *
     * @param sign the signature.
     * @return the matching recycle method or null.
     */
    public Method match(String[] sign)
    {
        if ((sign != null) && (sign.length > 0))
        {
            if ((signature != null) && (sign.length == signature.length))
            {
                for (int i = 0; i < signature.length; i++)
                {
                    if (!signature[i].equals(sign[i]))
                    {
                        return null;
                    }
                }
                return recycle;
            }
            else
            {
                return null;
            }
        }
        else if (signature == null)
        {
            return recycle;
        }
        else
        {
            return null;
        }
    }
}
