/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package test.java.util.concurrent.tck;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AtomicLongFieldUpdaterTest extends JSR166TestCase {
    volatile long x = 0;
    protected volatile long protectedField;
    private volatile long privateField;
    long w;
    float z;
    public static void main(String[] args) {
        main(suite(), args);
    }
    public static Test suite() {
        return new TestSuite(AtomicLongFieldUpdaterTest.class);
    }

    // for testing subclass access
    static class AtomicLongFieldUpdaterTestSubclass extends AtomicLongFieldUpdaterTest {
        public void checkPrivateAccess() {
            try {
                AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a =
                    AtomicLongFieldUpdater.newUpdater
                    (AtomicLongFieldUpdaterTest.class, "privateField");
                shouldThrow();
            } catch (RuntimeException success) {
                assertNotNull(success.getCause());
            }
        }

        public void checkCompareAndSetProtectedSub() {
            AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a =
                AtomicLongFieldUpdater.newUpdater
                (AtomicLongFieldUpdaterTest.class, "protectedField");
            this.protectedField = 1;
            assertTrue(a.compareAndSet(this, 1, 2));
            assertTrue(a.compareAndSet(this, 2, -4));
            assertEquals(-4, a.get(this));
            assertFalse(a.compareAndSet(this, -5, 7));
            assertEquals(-4, a.get(this));
            assertTrue(a.compareAndSet(this, -4, 7));
            assertEquals(7, a.get(this));
        }
    }

    static class UnrelatedClass {
        public void checkPackageAccess(AtomicLongFieldUpdaterTest obj) {
            obj.x = 72L;
            AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a =
                AtomicLongFieldUpdater.newUpdater
                (AtomicLongFieldUpdaterTest.class, "x");
            assertEquals(72L, a.get(obj));
            assertTrue(a.compareAndSet(obj, 72L, 73L));
            assertEquals(73L, a.get(obj));
        }

        public void checkPrivateAccess(AtomicLongFieldUpdaterTest obj) {
            try {
                AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a =
                    AtomicLongFieldUpdater.newUpdater
                    (AtomicLongFieldUpdaterTest.class, "privateField");
                throw new AssertionError("should throw");
            } catch (RuntimeException success) {
                assertNotNull(success.getCause());
            }
        }
    }

    AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> updaterFor(String fieldName) {
        return AtomicLongFieldUpdater.newUpdater
            (AtomicLongFieldUpdaterTest.class, fieldName);
    }

    /**
     * Construction with non-existent field throws RuntimeException
     */
    public void testConstructor() {
        try {
            updaterFor("y");
            shouldThrow();
        } catch (RuntimeException success) {
            assertNotNull(success.getCause());
        }
    }

    /**
     * construction with field not of given type throws IllegalArgumentException
     */
    public void testConstructor2() {
        try {
            updaterFor("z");
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * construction with non-volatile field throws IllegalArgumentException
     */
    public void testConstructor3() {
        try {
            updaterFor("w");
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * construction using private field from subclass throws RuntimeException
     */
    public void testPrivateFieldInSubclass() {
        AtomicLongFieldUpdaterTestSubclass s =
            new AtomicLongFieldUpdaterTestSubclass();
        s.checkPrivateAccess();
    }

    /**
     * construction from unrelated class; package access is allowed,
     * private access is not
     */
    public void testUnrelatedClassAccess() {
        new UnrelatedClass().checkPackageAccess(this);
        new UnrelatedClass().checkPrivateAccess(this);
    }

    /**
     * get returns the last value set or assigned
     */
    public void testGetSet() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.get(this));
        a.set(this, 2);
        assertEquals(2, a.get(this));
        a.set(this, -3);
        assertEquals(-3, a.get(this));
    }

    /**
     * get returns the last value lazySet by same thread
     */
    public void testGetLazySet() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.get(this));
        a.lazySet(this, 2);
        assertEquals(2, a.get(this));
        a.lazySet(this, -3);
        assertEquals(-3, a.get(this));
    }

    /**
     * compareAndSet succeeds in changing value if equal to expected else fails
     */
    public void testCompareAndSet() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertTrue(a.compareAndSet(this, 1, 2));
        assertTrue(a.compareAndSet(this, 2, -4));
        assertEquals(-4, a.get(this));
        assertFalse(a.compareAndSet(this, -5, 7));
        assertEquals(-4, a.get(this));
        assertTrue(a.compareAndSet(this, -4, 7));
        assertEquals(7, a.get(this));
    }

    /**
     * compareAndSet succeeds in changing protected field value if
     * equal to expected else fails
     */
    public void testCompareAndSetProtected() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("protectedField");
        protectedField = 1;
        assertTrue(a.compareAndSet(this, 1, 2));
        assertTrue(a.compareAndSet(this, 2, -4));
        assertEquals(-4, a.get(this));
        assertFalse(a.compareAndSet(this, -5, 7));
        assertEquals(-4, a.get(this));
        assertTrue(a.compareAndSet(this, -4, 7));
        assertEquals(7, a.get(this));
    }

    /**
     * compareAndSet succeeds in changing protected field value if
     * equal to expected else fails
     */
    public void testCompareAndSetProtectedInSubclass() {
        AtomicLongFieldUpdaterTestSubclass s =
            new AtomicLongFieldUpdaterTestSubclass();
        s.checkCompareAndSetProtectedSub();
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() throws Exception {
        x = 1;
        final AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");

        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!a.compareAndSet(AtomicLongFieldUpdaterTest.this, 2, 3))
                    Thread.yield();
            }});

        t.start();
        assertTrue(a.compareAndSet(this, 1, 2));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
        assertEquals(3, a.get(this));
    }

    /**
     * repeated weakCompareAndSet succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSet() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        do {} while (!a.weakCompareAndSet(this, 1, 2));
        do {} while (!a.weakCompareAndSet(this, 2, -4));
        assertEquals(-4, a.get(this));
        do {} while (!a.weakCompareAndSet(this, -4, 7));
        assertEquals(7, a.get(this));
    }

    /**
     * getAndSet returns previous value and sets to given value
     */
    public void testGetAndSet() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.getAndSet(this, 0));
        assertEquals(0, a.getAndSet(this, -10));
        assertEquals(-10, a.getAndSet(this, 1));
    }

    /**
     * getAndAdd returns previous value and adds given value
     */
    public void testGetAndAdd() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.getAndAdd(this, 2));
        assertEquals(3, a.get(this));
        assertEquals(3, a.getAndAdd(this, -4));
        assertEquals(-1, a.get(this));
    }

    /**
     * getAndDecrement returns previous value and decrements
     */
    public void testGetAndDecrement() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.getAndDecrement(this));
        assertEquals(0, a.getAndDecrement(this));
        assertEquals(-1, a.getAndDecrement(this));
    }

    /**
     * getAndIncrement returns previous value and increments
     */
    public void testGetAndIncrement() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(1, a.getAndIncrement(this));
        assertEquals(2, a.get(this));
        a.set(this, -2);
        assertEquals(-2, a.getAndIncrement(this));
        assertEquals(-1, a.getAndIncrement(this));
        assertEquals(0, a.getAndIncrement(this));
        assertEquals(1, a.get(this));
    }

    /**
     * addAndGet adds given value to current, and returns current value
     */
    public void testAddAndGet() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(3, a.addAndGet(this, 2));
        assertEquals(3, a.get(this));
        assertEquals(-1, a.addAndGet(this, -4));
        assertEquals(-1, a.get(this));
    }

    /**
     * decrementAndGet decrements and returns current value
     */
    public void testDecrementAndGet() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(0, a.decrementAndGet(this));
        assertEquals(-1, a.decrementAndGet(this));
        assertEquals(-2, a.decrementAndGet(this));
        assertEquals(-2, a.get(this));
    }

    /**
     * incrementAndGet increments and returns current value
     */
    public void testIncrementAndGet() {
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        a = updaterFor("x");
        x = 1;
        assertEquals(2, a.incrementAndGet(this));
        assertEquals(2, a.get(this));
        a.set(this, -2);
        assertEquals(-1, a.incrementAndGet(this));
        assertEquals(0, a.incrementAndGet(this));
        assertEquals(1, a.incrementAndGet(this));
        assertEquals(1, a.get(this));
    }

}
