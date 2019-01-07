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

import org.apache.fulcrum.testcontainer.BaseUnitTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Eric Pugh
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 *
 */
public class PoolServiceTest extends BaseUnitTest 
{
	/** Default pool service **/
	private PoolService poolService = null;

	/**
	 * Defines the testcase name for JUnit.
	 *
	 * @param name the testcase's name.
	 */
	public PoolServiceTest(String name) 
	{
		super(name);
	}

	/**
	 * Perform pool service setup
	 * 
	 * @throws Exception generic exception
	 */
	@Before
	public void setUp() throws Exception 
	{
		super.setUp();

		poolService = (PoolService) this.resolve(PoolService.class.getName());
	}

	/**
	 * Class to test for Object getInstance(Class)
	 * 
	 * @throws PoolException generic exception
	 */
	@Test
	public void testGetInstanceClass() throws PoolException 
	{
		Object object = poolService.getInstance(StringBuilder.class);
		assertTrue(object instanceof StringBuilder);

	}

	@Test
	public void testPutInstance() 
	{
		String s = "I am a string";
		assertEquals(0, poolService.getSize("java.lang.String"));
		poolService.putInstance(s);
		assertEquals(1, poolService.getSize("java.lang.String"));

	}

	@Test
	public void testGetSetCapacity() 
	{
		assertEquals(128, poolService.getCapacity("java.lang.String"));
		poolService.setCapacity("java.lang.String", 278);
		assertEquals(278, poolService.getCapacity("java.lang.String"));

	}

	@Test
	public void testGetSize() 
	{
		String s = "I am a string";
		assertEquals(0, poolService.getSize("java.lang.String"));
		poolService.putInstance(s);
		assertEquals(1, poolService.getSize("java.lang.String"));

	}

	/**
	 * Class to test for void clearPool(String)
	 */
	@Test
	public void testClearPoolString() 
	{
		String s = "I am a string";
		assertEquals(0, poolService.getSize("java.lang.String"));
		
		poolService.putInstance(s);
		assertEquals(1, poolService.getSize("java.lang.String"));
		
		poolService.clearPool("java.lang.String");
		assertEquals(0, poolService.getSize("java.lang.String"));
	}

	/*
	 * Class to test for void clearPool()
	 */
	@Test
	public void testClearPool() 
	{
		String s = "I am a string";
		assertEquals(0, poolService.getSize("java.lang.String"));
		
		poolService.putInstance(s);
		poolService.putInstance(new Double(32));
		assertEquals(1, poolService.getSize("java.lang.String"));
		
		poolService.clearPool();
		assertEquals(0, poolService.getSize("java.lang.String"));
		assertEquals(0, poolService.getSize("java.lang.Double"));
	}
}
