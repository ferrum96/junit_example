import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class MultiplicationTest {

    private double valueA;
    private double valueB;
    private double expected;

    public MultiplicationTest(double valueA, double valueB, double expected) {
        this.valueA = valueA;
        this.valueB = valueB;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "[{index}]:   {0} * {1} = {2}")
    public static Iterable<Object[]> dataForTest() {
        return Arrays.asList(new Object[][]{
                {3, 2, 6},
                {12.8, 6.25, 80},
                {18.35, 2.46, 45}
        });
    }

    @Test
    public void paramTest() {
        assertEquals(expected, new Calculator().multiplication(valueA,valueB), 0.00000001);
    }

}