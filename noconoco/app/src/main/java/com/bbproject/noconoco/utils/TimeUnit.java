package com.bbproject.noconoco.utils;

@SuppressWarnings("ALL")
public enum TimeUnit {
    NANOSECONDS {
        public long toNanos(long d) {
            return d;
        }

        public long toMicros(long d) {
            return d / (C1 / C0);
        }

        public long toMillis(long d) {
            return d / (C2 / C0);
        }

        public long toSeconds(long d) {
            return d / (C3 / C0);
        }

        public long toMinutes(long d) {
            return d / (C4 / C0);
        }

        public long toHours(long d) {
            return d / (C5 / C0);
        }

        public long toDays(long d) {
            return d / (C6 / C0);
        }

        public long convert(long d, TimeUnit u) {
            return u.toNanos(d);
        }

        int excessNanos(long d, long m) {
            return (int) (d - (m * C2));
        }
    },
    MICROSECONDS {
        public long toNanos(long d) {
            return x(d, C1 / C0, MAX / (C1 / C0));
        }

        public long toMicros(long d) {
            return d;
        }

        public long toMillis(long d) {
            return d / (C2 / C1);
        }

        public long toSeconds(long d) {
            return d / (C3 / C1);
        }

        public long toMinutes(long d) {
            return d / (C4 / C1);
        }

        public long toHours(long d) {
            return d / (C5 / C1);
        }

        public long toDays(long d) {
            return d / (C6 / C1);
        }

        public long convert(long d, TimeUnit u) {
            return u.toMicros(d);
        }

        int excessNanos(long d, long m) {
            return (int) ((d * C1) - (m * C2));
        }
    },
    MILLISECONDS {
        public long toNanos(long d) {
            return x(d, C2 / C0, MAX / (C2 / C0));
        }

        public long toMicros(long d) {
            return x(d, C2 / C1, MAX / (C2 / C1));
        }

        public long toMillis(long d) {
            return d;
        }

        public long toSeconds(long d) {
            return d / (C3 / C2);
        }

        public long toMinutes(long d) {
            return d / (C4 / C2);
        }

        public long toHours(long d) {
            return d / (C5 / C2);
        }

        public long toDays(long d) {
            return d / (C6 / C2);
        }

        public long convert(long d, TimeUnit u) {
            return u.toMillis(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    },
    SECONDS {
        public long toNanos(long d) {
            return x(d, C3 / C0, MAX / (C3 / C0));
        }

        public long toMicros(long d) {
            return x(d, C3 / C1, MAX / (C3 / C1));
        }

        public long toMillis(long d) {
            return x(d, C3 / C2, MAX / (C3 / C2));
        }

        public long toSeconds(long d) {
            return d;
        }

        public long toMinutes(long d) {
            return d / (C4 / C3);
        }

        public long toHours(long d) {
            return d / (C5 / C3);
        }

        public long toDays(long d) {
            return d / (C6 / C3);
        }

        public long convert(long d, TimeUnit u) {
            return u.toSeconds(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    },
    MINUTES {
        public long toNanos(long d) {
            return x(d, C4 / C0, MAX / (C4 / C0));
        }

        public long toMicros(long d) {
            return x(d, C4 / C1, MAX / (C4 / C1));
        }

        public long toMillis(long d) {
            return x(d, C4 / C2, MAX / (C4 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C4 / C3, MAX / (C4 / C3));
        }

        public long toMinutes(long d) {
            return d;
        }

        public long toHours(long d) {
            return d / (C5 / C4);
        }

        public long toDays(long d) {
            return d / (C6 / C4);
        }

        public long convert(long d, TimeUnit u) {
            return u.toMinutes(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    },
    HOURS {
        public long toNanos(long d) {
            return x(d, C5 / C0, MAX / (C5 / C0));
        }

        public long toMicros(long d) {
            return x(d, C5 / C1, MAX / (C5 / C1));
        }

        public long toMillis(long d) {
            return x(d, C5 / C2, MAX / (C5 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C5 / C3, MAX / (C5 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C5 / C4, MAX / (C5 / C4));
        }

        public long toHours(long d) {
            return d;
        }

        public long toDays(long d) {
            return d / (C6 / C5);
        }

        public long convert(long d, TimeUnit u) {
            return u.toHours(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    },
    DAYS {
        public long toNanos(long d) {
            return x(d, C6 / C0, MAX / (C6 / C0));
        }

        public long toMicros(long d) {
            return x(d, C6 / C1, MAX / (C6 / C1));
        }

        public long toMillis(long d) {
            return x(d, C6 / C2, MAX / (C6 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C6 / C3, MAX / (C6 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C6 / C4, MAX / (C6 / C4));
        }

        public long toHours(long d) {
            return x(d, C6 / C5, MAX / (C6 / C5));
        }

        public long toDays(long d) {
            return d;
        }

        public long convert(long d, TimeUnit u) {
            return u.toDays(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    };

    // Handy constants for conversion methods
    static final long C0 = 1L;
    static final long C1 = C0 * 1000L;
    static final long C2 = C1 * 1000L;
    static final long C3 = C2 * 1000L;
    static final long C4 = C3 * 60L;
    static final long C5 = C4 * 60L;
    static final long C6 = C5 * 24L;

    static final long MAX = Long.MAX_VALUE;

    /**
     * Scale d by m, checking for overflow.
     * This has a short name to make above code more readable.
     */
    static long x(long d, long m, long over) {
        if (d > over) return Long.MAX_VALUE;
        if (d < -over) return Long.MIN_VALUE;
        return d * m;
    }

    /**
     * Convert the given time duration in the given unit to this
     * unit.
     */
    public long convert(long sourceDuration, TimeUnit sourceUnit) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to {@code NANOSECONDS.convert(duration, this)}.
     *
     * @see #convert
     */
    public long toNanos(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to {@code MICROSECONDS.convert(duration, this)}.
     *
     * @see #convert
     */
    public long toMicros(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to {@code MILLISECONDS.convert(duration, this)}.
     *
     * @see #convert
     */
    public long toMillis(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to {@code SECONDS.convert(duration, this)}.
     *
     * @see #convert
     */
    public long toSeconds(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to {@code MINUTES.convert(duration, this)}.
     *
     * @see #convert
     */
    public long toMinutes(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to {@code HOURS.convert(duration, this)}.
     *
     * @see #convert
     */
    public long toHours(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to {@code DAYS.convert(duration, this)}.
     *
     * @see #convert
     */
    public long toDays(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Utility to compute the excess-nanosecond argument to icon_wait,
     * sleep, join.
     *
     * @param d the duration
     * @param m the number of milliseconds
     * @return the number of nanoseconds
     */
    abstract int excessNanos(long d, long m);

    /**
     * Performs a timed {@link Object#wait(long, int) Object.icon_wait}
     * using this time unit.
     * This is a convenience method that converts timeout arguments
     * into the form required by the {@code Object.icon_wait} method.
     *
     * @param obj     the object to icon_wait on
     * @param timeout the maximum time to icon_wait. If less than
     *                or equal to zero, do not icon_wait at all.
     * @throws InterruptedException if interrupted while waiting
     */
    public void timedWait(Object obj, long timeout)
            throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            int ns = excessNanos(timeout, ms);
            obj.wait(ms, ns);
        }
    }

    /**
     * Performs a timed {@link Thread#join(long, int) Thread.join}
     * using this time unit.
     * This is a convenience method that converts time arguments into the
     * form required by the {@code Thread.join} method.
     *
     * @param thread  the thread to icon_wait for
     * @param timeout the maximum time to icon_wait. If less than
     *                or equal to zero, do not icon_wait at all.
     * @throws InterruptedException if interrupted while waiting
     */
    public void timedJoin(Thread thread, long timeout)
            throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            int ns = excessNanos(timeout, ms);
            thread.join(ms, ns);
        }
    }

    /**
     * Performs a {@link Thread#sleep(long, int) Thread.sleep} using
     * this time unit.
     * This is a convenience method that converts time arguments into the
     * form required by the {@code Thread.sleep} method.
     *
     * @param timeout the minimum time to sleep. If less than
     *                or equal to zero, do not sleep at all.
     * @throws InterruptedException if interrupted while sleeping
     */
    public void sleep(long timeout) throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            int ns = excessNanos(timeout, ms);
            Thread.sleep(ms, ns);
        }
    }

}
