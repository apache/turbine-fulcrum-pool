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
 * Efficient array-based bounded buffer class.
 * Adapted from CPJ, chapter 8, which describes design.
 * Originally written by Doug Lea and released into the public domain.
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>] <p>
 *
 * @author <a href="mailto:ilkka.priha@simsoft.fi">Ilkka Priha</a>
 * @version $Id$
 */
public class BoundedBuffer
{
    /**
     * The default capacity.
     */
    public static final int DEFAULT_CAPACITY = 1024;

    protected final Object[]  array_;      // the elements

    protected int takePtr_ = 0;            // circular indices
    protected int putPtr_ = 0;

    protected int usedSlots_ = 0;          // length
    protected int emptySlots_;             // capacity - length

    /**
     * Creates a buffer with the given capacity.
     *
     * @param capacity the capacity.
     * @throws IllegalArgumentException if capacity less or equal to zero.
     */
    public BoundedBuffer(int capacity)
                         throws IllegalArgumentException
    {
        if (capacity <= 0)
           throw new IllegalArgumentException();

        array_ = new Object[capacity];
        emptySlots_ = capacity;
    }

    /**
     * Creates a buffer with the default capacity
     */
    public BoundedBuffer()
    {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Returns the number of elements in the buffer.
     * This is only a snapshot value, that may change
     * immediately after returning.
     *
     * @return the size.
     */
    public synchronized int size()
    {
        return usedSlots_;
    }

    /**
     * Returns the capacity of the buffer.
     *
     * @return the capacity.
     */
    public int capacity()
    {
        return array_.length;
    }

    /**
     * Peeks, but does not remove the top item from the buffer.
     *
     * @return the object or null.
     */
    public synchronized Object peek()
    {
        if (usedSlots_ > 0)
            return array_[takePtr_];
        else
            return null;
    }

    /**
     * Puts an item in the buffer only if there is capacity available.
     *
     * @param x the item to be inserted.
     * @return true if accepted, else false.
     */
    public synchronized boolean offer(Object x)
    {
        if (x == null)
            throw new IllegalArgumentException();

        if (emptySlots_ > 0)
        {
            --emptySlots_;
            array_[putPtr_] = x;
            if (++putPtr_ >= array_.length)
                putPtr_ = 0;
            usedSlots_++;
            return true;
        }
        else
            return false;
    }

    /**
     * Polls and removes the top item from the buffer if one is available.
     *
     * @return the oldest item from the buffer, or null if the buffer is empty.
     */
    public synchronized Object poll()
    {
        if (usedSlots_ > 0)
        {
            --usedSlots_;
            Object old = array_[takePtr_];
            array_[takePtr_] = null;
            if (++takePtr_ >= array_.length)
                takePtr_ = 0;
            emptySlots_++;
            return old;
        }
        else
            return null;
    }
}
