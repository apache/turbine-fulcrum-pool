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

/**
 * The Pool Service extends the Factory Service by adding support for pooling
 * instantiated objects. When a new instance is requested, the service first
 * checks its pool if one is available. If the the pool is empty, a new object
 * will be instantiated from the specified class. If only class name is given,
 * the request to create an instance will be forwarded to the Factory Service.
 *
 * <p>
 * For objects implementing the Recyclable interface, a recycle method will be
 * called, when they are taken from the pool, and a dispose method, when they
 * are returned to the pool.
 * </p>
 *
 * @author <a href="mailto:ilkka.priha@simsoft.fi">Ilkka Priha</a>
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Id$
 */
public interface PoolService 
{
	/** Avalon role - used to id the component within the manager */
	String ROLE = PoolService.class.getName();

	/**
	 * The default pool capacity.
	 */
	public static final int DEFAULT_POOL_CAPACITY = 128;

	/**
	 * Gets an instance of a specified class either from the pool or by
	 * instantiating from the class if the pool is empty.
	 *
	 * @param clazz the class.
	 * @return the instance.
	 * @throws PoolException if recycling fails.
	 */
	public Object getInstance(Class clazz) throws PoolException;

	/**
	 * Gets an instance of a specified class either from the pool or by
	 * instantiating from the class if the pool is empty.
	 *
	 * @param clazz     the class.
	 * @param params    an array containing the parameters of the constructor.
	 * @param signature an array containing the signature of the constructor.
	 * @return the instance.
	 * @throws PoolException if recycling fails.
	 */
	public Object getInstance(Class clazz, Object params[], String signature[]) throws PoolException;

	/**
	 * Puts a used object back to the pool. Objects implementing the Recyclable
	 * interface can provide a recycle method to be called when they are reused and
	 * a dispose method to be called when they are returned to the pool.
	 *
	 * @param instance the object instance to recycle.
	 * @return true if the instance was accepted.
	 */
	public boolean putInstance(Object instance);

	/**
	 * Gets the capacity of the pool for a named class.
	 *
	 * @param className the name of the class.
	 * @return total capacity
	 */
	public int getCapacity(String className);

	/**
	 * Sets the capacity of the pool for a named class. Note that the pool will be
	 * cleared after the change.
	 *
	 * @param className the name of the class.
	 * @param capacity  the new capacity.
	 */
	public void setCapacity(String className, int capacity);

	/**
	 * Gets the current size of the pool for a named class.
	 *
	 * @param className the name of the class
	 * @return the size of the pool for the class
	 */
	public int getSize(String className);

	/**
	 * Clears instances of a named class from the pool.
	 *
	 * @param className the name of the class.
	 */
	public void clearPool(String className);

	/**
	 * Clears all instances from the pool.
	 */
	void clearPool();

}
