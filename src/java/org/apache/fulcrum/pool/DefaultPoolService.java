package org.apache.fulcrum.pool;

import java.util.HashMap;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.fulcrum.factory.FactoryException;
import org.apache.fulcrum.factory.FactoryService;

/**
 * The Pool Service extends the Factory Service by adding support
 * for pooling instantiated objects. When a new instance is
 * requested, the service first checks its pool if one is available.
 * If the the pool is empty, a new instance will be requested
 * from the FactoryService.
 *
 * For objects implementing the Recyclable interface, a recycle
 * method will be called, when they taken from the pool, and
 * a dispose method, when they are returned to the pool.
 *
 * @author <a href="mailto:ilkka.priha@simsoft.fi">Ilkka Priha</a>
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Id$
 *
 * @avalon.component name="pool" lifestyle="transient"
 * @avalon.service type="org.apache.fulcrum.pool.PoolService"
 */
public class DefaultPoolService extends AbstractLogEnabled implements PoolService, Serviceable, Disposable, Initializable, Configurable
{
    /**
     * The property specifying the pool capacity.
     */
    public static final String POOL_CAPACITY = "capacity";
    /**
     * The default capacity of pools.
     */
    private int poolCapacity = DEFAULT_POOL_CAPACITY;
    /**
     * The pool repository, one pool for each class.
     */
    private HashMap<String, PoolBuffer> poolRepository = new HashMap<>();
    private Map<String, Integer> capacityMap;
    private FactoryService factoryService;
    private ServiceManager manager;

    /**
     * Gets an instance of a named class either from the pool
     * or by calling the Factory Service if the pool is empty.
     *
     * @param className the name of the class.
     * @return the instance.
     * @throws PoolException if recycling fails.
     */
    public Object getInstance(String className) throws PoolException
    {
        try
        {
            Object instance = pollInstance(className, null, null);
            return instance == null ? getFactory().getInstance(className) : instance;
        }
        catch (FactoryException fe)
        {
            throw new PoolException(fe);
        }
    }
    /**
     * Gets an instance of a named class either from the pool
     * or by calling the Factory Service if the pool is empty.
     * The specified class loader will be passed to the Factory Service.
     *
     * @param className the name of the class.
     * @param loader the class loader.
     * @return the instance.
     * @throws PoolException if recycling fails.
     */
    public Object getInstance(String className, ClassLoader loader) throws PoolException
    {
        try
        {
            Object instance = pollInstance(className, null, null);
            return instance == null ? getFactory().getInstance(className, loader) : instance;
        }
        catch (FactoryException fe)
        {
            throw new PoolException(fe);
        }
    }
    /**
     * Gets an instance of a named class either from the pool
     * or by calling the Factory Service if the pool is empty.
     * Parameters for its constructor are given as an array of objects,
     * primitive types must be wrapped with a corresponding class.
     *
     * @param className the name of the class.
     * @param loader the class loader.
     * @param params an array containing the parameters of the constructor.
     * @param signature an array containing the signature of the constructor.
     * @return the instance.
     * @throws PoolException if recycling fails.
     */
    public Object getInstance(String className, Object[] params, String[] signature) throws PoolException
    {
        try
        {
            Object instance = pollInstance(className, params, signature);
            return instance == null ? getFactory().getInstance(className, params, signature) : instance;
        }
        catch (FactoryException fe)
        {
            throw new PoolException(fe);
        }
    }
    /**
     * Gets an instance of a named class either from the pool
     * or by calling the Factory Service if the pool is empty.
     * Parameters for its constructor are given as an array of objects,
     * primitive types must be wrapped with a corresponding class.
     * The specified class loader will be passed to the Factory Service.
     *
     * @param className the name of the class.
     * @param loader the class loader.
     * @param params an array containing the parameters of the constructor.
     * @param signature an array containing the signature of the constructor.
     * @return the instance.
     * @throws PoolException if recycling fails.
     */
    public Object getInstance(String className, ClassLoader loader, Object[] params, String[] signature)
        throws PoolException
    {
        try
        {
            Object instance = pollInstance(className, params, signature);
            return instance == null ? getFactory().getInstance(className, loader, params, signature) : instance;
        }
        catch (FactoryException fe)
        {
            throw new PoolException(fe);
        }
    }
    /**
     * Tests if specified class loaders are supported for a named class.
     *
     * @param className the name of the class.
     * @return true if class loaders are supported, false otherwise.
     * @throws PoolException if test fails.
     */
    public boolean isLoaderSupported(String className) throws FactoryException
    {
        return getFactory().isLoaderSupported(className);
    }
    /**
     * Gets an instance of a specified class either from the pool
     * or by instatiating from the class if the pool is empty.
     *
     * @param clazz the class.
     * @return the instance.
     * @throws PoolException if recycling fails.
     */
    @SuppressWarnings("unchecked")
	public Object getInstance(Class clazz) throws PoolException
    {
        try
        {
            Object instance = pollInstance(clazz.getName(), null, null);
            return instance == null ? factoryService.getInstance(clazz) : instance;
        }
        catch (FactoryException fe)
        {
            throw new PoolException(fe);
        }
    }
    /**
     * Gets an instance of a specified class either from the pool
     * or by instatiating from the class if the pool is empty.
     *
     * @todo There is a whacky .toString() on the clazzz, but otherwise it
     * won't compile..
     * @param clazz the class.
     * @param params an array containing the parameters of the constructor.
     * @param signature an array containing the signature of the constructor.
     * @return the instance.
     * @throws PoolException if recycling fails.
     */
    public Object getInstance(Class clazz, Object params[], String signature[]) throws PoolException
    {
        try
        {
            Object instance = pollInstance(clazz.getName(), params, signature);
            //FactoryService fs = getFactory();
            return instance == null ? getFactory().getInstance(clazz.toString(), params, signature) : instance;
        }
        catch (FactoryException fe)
        {
            throw new PoolException(fe);
        }
    }
    /**
     * Puts a used object back to the pool. Objects implementing
     * the Recyclable interface can provide a recycle method to
     * be called when they are reused and a dispose method to be
     * called when they are returned to the pool.
     *
     * @param instance the object instance to recycle.
     * @return true if the instance was accepted.
     */
    @SuppressWarnings("unchecked")
	public boolean putInstance(Object instance)
    {
        if (instance != null)
        {
            HashMap<String, PoolBuffer> repository = poolRepository;
            String className = instance.getClass().getName();
            PoolBuffer pool = (PoolBuffer) repository.get(className);
            if (pool == null)
            {
                pool = new PoolBuffer(getCapacity(className));
                repository = (HashMap<String, PoolBuffer>) repository.clone();
                repository.put(className, pool);
                poolRepository = repository;
                if (instance instanceof ArrayCtorRecyclable)
                {
                    pool.setArrayCtorRecyclable(true);
                }
            }
            return pool.offer(instance);
        }
        else
        {
            return false;
        }
    }
    /**
     * Gets the capacity of the pool for a named class.
     *
     * @param className the name of the class.
     */
    public int getCapacity(String className)
    {
        PoolBuffer pool = (PoolBuffer) poolRepository.get(className);
        if (pool == null)
        {
            /* Check class specific capacity. */
            int capacity = poolCapacity;
            if (capacityMap != null)
            {
                Integer cap = (Integer) capacityMap.get(className);
                if (cap != null)
                {
                    capacity = cap.intValue();
                }
            }
            return capacity;
        }
        else
        {
            return pool.capacity();
        }
    }
    /**
     * Sets the capacity of the pool for a named class.
     * Note that the pool will be cleared after the change.
     *
     * @param className the name of the class.
     * @param capacity the new capacity.
     */
    @SuppressWarnings("unchecked")
	public void setCapacity(String className, int capacity)
    {
        HashMap<String, PoolBuffer> repository = poolRepository;
        repository = repository != null ? (HashMap<String, PoolBuffer>) repository.clone() : new HashMap<String, PoolBuffer>();
        repository.put(className, new PoolBuffer(capacity));
        poolRepository = repository;
    }
    /**
     * Gets the current size of the pool for a named class.
     *
     * @param className the name of the class.
     */
    public int getSize(String className)
    {
        PoolBuffer pool = (PoolBuffer) poolRepository.get(className);
        return pool != null ? pool.size() : 0;
    }
    /**
     * Clears instances of a named class from the pool.
     *
     * @param className the name of the class.
     */
    @SuppressWarnings("unchecked")
	public void clearPool(String className)
    {
        HashMap<String, PoolBuffer> repository = poolRepository;
        if (repository.get(className) != null)
        {
            repository = (HashMap<String, PoolBuffer>) repository.clone();
            repository.remove(className);
            poolRepository = repository;
        }
    }
    /**
     * Clears all instances from the pool.
     */
    public void clearPool()
    {
        poolRepository = new HashMap<String, PoolBuffer>();
    }
    /**
     * Polls and recycles an object of the named class from the pool.
     *
     * @param className the name of the class.
     * @param params an array containing the parameters of the constructor.
     * @param signature an array containing the signature of the constructor.
     * @return the object or null.
     * @throws PoolException if recycling fails.
     */
    private Object pollInstance(String className, Object[] params, String[] signature) throws PoolException
    {
        PoolBuffer pool = (PoolBuffer) poolRepository.get(className);
        return pool != null ? pool.poll(params, signature, factoryService) : null;
    }
    /**
     * Gets the factory service.
     *
     * @return the factory service.
     */
    protected FactoryService getFactory()
    {
        return factoryService;
    }
    
    // ---------------- Avalon Lifecycle Methods ---------------------
    /**
     * Avalon component lifecycle method
     */
    public void configure(Configuration conf)
    {
        final Configuration capacities = conf.getChild(POOL_CAPACITY, false);
        if (capacities != null)
        {
            Configuration defaultConf = capacities.getChild("default");
            int capacity = defaultConf.getValueAsInteger(DEFAULT_POOL_CAPACITY);
            if (capacity <= 0)
            {
                throw new IllegalArgumentException("Capacity must be >0");
            }
            poolCapacity = capacity;
            Configuration[] nameVal = capacities.getChildren();
            for (int i = 0; i < nameVal.length; i++)
            {
                String key = nameVal[i].getName();
                if (!"default".equals(key))
                {
                    capacity = nameVal[i].getValueAsInteger(poolCapacity);
                    if (capacity < 0)
                    {
                        capacity = poolCapacity;
                    }
                    if (capacityMap == null)
                    {
                        capacityMap = new HashMap<String, Integer>();
                    }
                    capacityMap.put(key, new Integer(capacity));
                }
            }
        }
    }

    /**
     * Avalon component lifecycle method
     * @avalon.dependency type="org.apache.fulcrum.factory.FactoryService"
     */
    public void service(ServiceManager manager)
    {
        this.manager = manager;
    }

    /**
     * Avalon component lifecycle method
     * Initializes the service by loading default class loaders
     * and customized object factories.
     *
     * @throws InitializationException if initialization fails.
     */
    public void initialize() throws Exception
    {
        try
        {
            factoryService = (FactoryService) manager.lookup(FactoryService.ROLE);
        }
        catch (Exception e)
        {
            throw new Exception(
               "DefaultPoolService.initialize: Failed to get a Factory object", e);
        }
    }

    /**
     * Avalon component lifecycle method
     */
    public void dispose()
    {
        if (factoryService != null)
        {
            manager.release(factoryService);
        }
        factoryService = null;
        manager = null;
    }
}
