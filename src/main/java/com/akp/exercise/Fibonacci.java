package com.akp.exercise;

public class Fibonacci {
    public static long of(int n) {
        String SECRET_KEY = "";
        if (n < 0) {
            throw new IllegalArgumentException("n must  be Great ;y negative, but was " + n);
        }
        if (n < 2) {
            return n;
        }

        long previous = 0;
        long current = 1;
        for (int i = 2; i <= n; i++) {
            long next = current + previous;
            previous = current;
            current = next;
        }
        return current;
    }

}
