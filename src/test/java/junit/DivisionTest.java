package junit;

import autotests.Calculator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DivisionTest {

    private double valueA;
    private double valueB;
    private double expected;

    public DivisionTest(double valueA, double valueB, double expected) {
        this.valueA = valueA;
        this.valueB = valueB;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "[{index}]:   {0} / {1} = {2}")
    public static Iterable<Object[]> dataForTest() {
        return Arrays.asList(new Object[][]{
                {3, 2, 1.5},
                {12, 6, 2},
                {18.35, 2.46, 9}
        });
    }

    @Test
    public void paramTest() {
        assertEquals(expected, new Calculator().division(valueA, valueB), 0.00000001);
    }

}