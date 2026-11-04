import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.akp.exercise.DurationFormatter;

/**
 * 
 * 0 → "now"
 * 1 → "1 second"
 * 2 -> "2 seconds"
 * 62 → "1 minute and 2 seconds"
 * 3662 → "1 hour, 1 minute and 2 seconds"
 * negative - throw IllegalArguemnt
 */
class DurationFormatterTest {
    private DurationFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new DurationFormatter();
    }

    @Test
    void zeroDurationFormatsAsNow() {
        assertEquals("now", formatter.format(0));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1 second",
            "2, 2 seconds",
            "60, 1 minute",
            "120, 2 minutes",
            "62, 1 minute and 2 seconds",
            "3600, 1 hour",
            "3660, 1 hour and 1 minute",
            "3662, '1 hour, 1 minute and 2 seconds'"
    })
    void formatSecondsToHumanReadableFormat(int input, String expected) {
        assertEquals(expected, formatter.format(input));
    }

    @Test
    void throwIllegalArgumentExceptionForNegativevalue() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(-1));
        assertEquals("Seconds must not be negative but was -1", exception.getMessage());
    }

}
