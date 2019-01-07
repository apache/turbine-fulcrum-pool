package org.apache.fulcrum.pool;

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

import java.lang.reflect.Method;
import java.util.ArrayList;

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
	 * Tells pool that it contains objects which can be initialized using an Object
	 * array.
	 *
	 * @param isArrayCtor a <code>boolean</code> value
	 */
	public void setArrayCtorRecyclable(boolean isArrayCtor) 
	{
		arrayCtorRecyclable = isArrayCtor;
	}

	/**
	 * Polls for an instance from the pool.
	 * 
	 * 
	 * @param params         object paramaters
	 * @param signature      signature of the class
	 * @param factoryService service to add
	 * @throws PoolException if service failed to be found
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
			} else if (instance instanceof Recyclable) {
				try 
				{
					if (signature != null && signature.length > 0) 
					{
						/* Get the recycle method from the cache. */
						Method recycle = getRecycle(signature);
						if (recycle == null) 
						{
							synchronized (this) {
								/* Make a synchronized recheck. */
								recycle = getRecycle(signature);
								if (recycle == null) 
								{
									Class<? extends Object> clazz = instance.getClass();
									recycle = clazz.getMethod("recycle",
											factoryService.getSignature(clazz, params, signature));

									@SuppressWarnings("unchecked")
									ArrayList<Recycler> cache = recyclers != null
											? (ArrayList<Recycler>) recyclers.clone()
											: new ArrayList<Recycler>();
									cache.add(new Recycler(recycle, signature));
									recyclers = cache;
								}
							}
						}
						recycle.invoke(instance, params);
					} else {
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
	 * @return false if failed to dispose
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
	 * Returns a cached recycle method corresponding to the given signature.
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
			for (Recycler recycler : cache) 
			{
				recycle = recycler.match(signature);
				if (recycle != null)
					return recycle;
			}
		}
		return null;
	}
}
