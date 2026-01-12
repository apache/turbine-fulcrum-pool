package org.apache.fulcrum.pool;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.apache.fulcrum.testcontainer.BaseUnit5Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Basic testing of the PoolService Component
 *
 * @author <a href="mailto:painter@apache.org">Jeffery Painter</a>
 * @author Eric Pugh
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 *
 */
public class PoolServiceTest extends BaseUnit5Test
{
	/** Default pool service **/
	private PoolService poolService = null;

	/**
	 * Perform pool service setup
	 *
	 * @throws Exception generic exception
	 */
	@BeforeEach
	public void setUp() throws Exception
	{
        setConfigurationFileName("src/test/TestComponentConfig.xml");
        setRoleFileName("src/test/TestRoleConfig.xml");
        poolService = (PoolService) this.lookup(PoolService.class.getName());
	}

	/**
	 * Class to test for Object getInstance(Class)
	 *
	 * @throws PoolException generic exception
	 */
	@Test
	public void testGetInstanceClass() throws PoolException
	{
        Object object1 = poolService.getInstance(StringBuilder.class);
        assertInstanceOf(StringBuilder.class, object1);

        String sourceValue = "testing";
        Object params[] = new Object[] { sourceValue };
        String signature[] = new String[] { "java.lang.String" };
        Object object2 = poolService.getInstance(StringBuilder.class, params, signature);
        assertInstanceOf(StringBuilder.class, object2);
        assertEquals(sourceValue, object2.toString());
	}

	/**
	 * Test adding an instance to the pool
	 */
	@Test
	public void testPutInstance()
	{
		String s = "I am a string";
		assertEquals(0, poolService.getSize("java.lang.String"));
		poolService.putInstance(s);
		assertEquals(1, poolService.getSize("java.lang.String"));

	}

	/**
	 * Test altering pool capacity
	 */
	@Test
	public void testGetSetCapacity()
	{
		assertEquals(128, poolService.getCapacity("java.lang.String"));
		poolService.setCapacity("java.lang.String", 278);
		assertEquals(278, poolService.getCapacity("java.lang.String"));

	}

	/**
	 * Test to determine current size of the pool
	 */
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

	/**
	 * Class to test for void clearPool()
	 */
	@Test
	public void testClearPool()
	{
		String s = "I am a string";
		assertEquals(0, poolService.getSize("java.lang.String"));

		poolService.putInstance(s);
		poolService.putInstance(Double.valueOf(32));
		assertEquals(1, poolService.getSize("java.lang.String"));

		poolService.clearPool();
		assertEquals(0, poolService.getSize("java.lang.String"));
		assertEquals(0, poolService.getSize("java.lang.Double"));
	}
}
