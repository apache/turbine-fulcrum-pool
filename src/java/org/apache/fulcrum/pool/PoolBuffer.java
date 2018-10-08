package org.apache.fulcrum.pool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.fulcrum.factory.FactoryService;


/**
 * An inner class for class specific pools.
 */
public class PoolBuffer
{
    /**
     * A buffer for class instances.
     */
    private BoundedBuffer pool;
    /**
     * A flag to determine if a more efficient recycler is implemented.
     */
    private boolean arrayCtorRecyclable;
    /**
     * A cache for recycling methods.
     */
    private ArrayList<Recycler> recyclers;
    /**
     * Contructs a new pool buffer with a specific capacity.
     *
     * @param capacity a capacity.
     */
    public PoolBuffer(int capacity)
    {
        pool = new BoundedBuffer(capacity);
    }
    /**
     * Tells pool that it contains objects which can be
     * initialized using an Object array.
     *
     * @param isArrayCtor a <code>boolean</code> value
     */
    public void setArrayCtorRecyclable(boolean isArrayCtor)
    {
        arrayCtorRecyclable = isArrayCtor;
    }
    /**
     * Polls for an instance from the pool.
     * @param factoryService 
     *
     * @return an instance or null.
     */
    public Object poll(Object[] params, String[] signature, FactoryService factoryService) throws PoolException
    {
        Object instance = pool.poll();
        if (instance != null)
        {
            if (arrayCtorRecyclable)
            {
                ((ArrayCtorRecyclable) instance).recycle(params);
            }
            else if (instance instanceof Recyclable)
            {
                try
                {
                    if ((signature != null) && (signature.length > 0))
                    {
                        /* Get the recycle method from the cache. */
                        Method recycle = getRecycle(signature);
                        if (recycle == null)
                        {
                            synchronized (this)
                            {
                                /* Make a synchronized recheck. */
                                recycle = getRecycle(signature);
                                if (recycle == null)
                                {
                                    Class<? extends Object> clazz = instance.getClass();
                                    recycle =
                                        clazz.getMethod(
                                            "recycle",
                                            factoryService.getSignature(clazz, params, signature));
                                    ArrayList<Recycler> cache =
                                        recyclers != null ? (ArrayList<Recycler>) recyclers.clone() : new ArrayList<Recycler>();
                                    cache.add(new Recycler(recycle, signature));
                                    recyclers = cache;
                                }
                            }
                        }
                        recycle.invoke(instance, params);
                    }
                    else
                    {
                        ((Recyclable) instance).recycle();
                    }
                }
                catch (Exception x)
                {
                    throw new PoolException("Recycling failed for " + instance.getClass().getName(), x);
                }
            }
        }
        return instance;
    }
    /**
     * Offers an instance to the pool.
     *
     * @param instance an instance.
     */
    public boolean offer(Object instance)
    {
        if (instance instanceof Recyclable)
        {
            try
            {
                ((Recyclable) instance).dispose();
            }
            catch (Exception x)
            {
                return false;
            }
        }
        return pool.offer(instance);
    }
    /**
     * Returns the capacity of the pool.
     *
     * @return the capacity.
     */
    public int capacity()
    {
        return pool.capacity();
    }
    /**
     * Returns the size of the pool.
     *
     * @return the size.
     */
    public int size()
    {
        return pool.size();
    }
    /**
     * Returns a cached recycle method
     * corresponding to the given signature.
     *
     * @param signature the signature.
     * @return the recycle method or null.
     */
    private Method getRecycle(String[] signature)
    {
        ArrayList<Recycler> cache = recyclers;
        if (cache != null)
        {
            Method recycle;
            for (Iterator<Recycler> i = cache.iterator(); i.hasNext();)
            {
                recycle = ((Recycler) i.next()).match(signature);
                if (recycle != null)
                {
                    return recycle;
                }
            }
        }
        return null;
    }
}
