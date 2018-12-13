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
import java.util.Arrays;

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
        signature = sign != null && sign.length > 0 ? sign : null;
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
    	if ( signature == null )
    	{
    		return recycle;
    	} else {

    		// test if there is a match 
	    	if ( !Arrays.equals(sign,  signature) ) {
	    		return null;
	    	} else {
	    		return recycle;
	    	}
    	}
    }
}
