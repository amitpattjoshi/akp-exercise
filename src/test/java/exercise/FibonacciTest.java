package exercise;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

import com.akp.exercise.Fibonacci;

/**
 * n: 0 1 2 3 4 5 6 7
 * fib(n): 0 1 1 2 3 5 8 13
 * 
 * FibonacciTest
 */
class FibonacciTest {

    // @Test
    // void fibonacciOfZeroIsZero() {
    // assertEquals(0, Fibonacci.of(0));
    // }

    // @Test
    // void fibonacciOfOneIsOne() {
    // assertEquals(1, Fibonacci.of(1));
    // }

    // @Test
    // void fibonacciOfTwoIsOne() {
    // assertEquals(1, Fibonacci.of(2));
    // }

    // @Test
    // void fibonacciOfThreeIsTwo() {
    // assertEquals(2, Fibonacci.of(3));
    // }

    @ParameterizedTest(name = "fib{0} = {1}")
    @CsvSource({
            "0, 0",
            "1, 1",
            "2, 1",
            "3, 2",
            "5, 5"
    })
    void returnNthFibonacciNumber(int n, int expected) {
        assertEquals(expected, Fibonacci.of(n));
    }

    @Test
    void fibonacciOfNegativeNumberThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Fibonacci.of(-1));
    }

    @Test
    void largeInputIsCalculatedQuickly() {
        assertTimeoutPreemptively(Duration.ofMillis(100),
                () -> assertEquals(1134903170, Fibonacci.of(45)));
    }

    @Test
    void largeInputIsCalculatedQuicklyFor46() {
        assertTimeoutPreemptively(Duration.ofMillis(100),
                () -> assertEquals(2971215073l, Fibonacci.of(47)));
    }
}
