package junit;

import autotests.Calculator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class AdditionTest {

    private double valueA;
    private double valueB;
    private double expected;

    public AdditionTest(double valueA, double valueB, double expected) {
        this.valueA = valueA;
        this.valueB = valueB;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "[{index}]:   {0} + {1} = {2}")
    public static Iterable<Object[]> dataForTest() {
        return Arrays.asList(new Object[][]{
                {1.5, 1, 2.5},
                {2, 6, 8},
                {18, 2, 20},
                {13, 15, 28},
                {1, 5, 6}
        });
    }

    @Test
    public void paramTest() {
        assertEquals(expected, new Calculator().addition(valueA,valueB), 0.00000001);
    }

}
