import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class SubtractionTest {

    private double valueA;
    private double valueB;
    private double expected;

    public SubtractionTest(double valueA, double valueB, double expected) {
        this.valueA = valueA;
        this.valueB = valueB;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "[{index}]:   {0} - {1} = {2}")
    public static Iterable<Object[]> dataForTest() {
        return Arrays.asList(new Object[][]{
                {3, 2, 6},
                {12.8, 6.25, 6.55},
                {18.35, 2.46, 15.89},
                {8, 6, 2}
        });
    }

    @Test
    public void paramTest() {
        assertEquals(expected, new Calculator().subtraction(valueA,valueB), 0.00000001);
    }

}