package autotests.glue;

import autotests.Calculator;
import cucumber.api.java.ru.Допустим;

import static org.junit.Assert.assertEquals;

public class Steps {

    @Допустим("[{int} + {int} = {int}]")
    public void сложениеЦелых(int valA, int valB, int result) {
        assertEquals(result, new Calculator().addition(valA, valB));
    }

    @Допустим("[{double} + {double} = {double}]")
    public void сложение(double valA, double valB, double result) {
        assertEquals(result, new Calculator().addition(valA, valB), 0.000000000000001);
    }

    @Допустим("[{double} - {double} = {double}]")
    public void вычитание(double valA, double valB, double result) {
        assertEquals(result, new Calculator().subtraction(valA, valB), 0.000000000000001);
    }

    @Допустим("[{int} - {int} = {int}]")
    public void вычитаниеЦелых(int valA, int valB, int result) {
        assertEquals(result, new Calculator().subtraction(valA, valB));
    }

    @Допустим("[{double} * {double} = {double}]")
    public void умножение(double valA, double valB, double result) {
        assertEquals(result, new Calculator().multiplication(valA, valB), 0.0000000000000001);
    }

    @Допустим("[{int} * {int} = {int}]")
    public void умножениеЦелых(int valA, int valB, int result) {
        assertEquals(result, new Calculator().multiplication(valA, valB));
    }

    @Допустим("[{double} div {double} = {double}]")
    public void деление(double valA, double valB, double result) {
        assertEquals(result, new Calculator().division(valA, valB), 0.000000000000000001);
    }

    @Допустим("[{int} div {int} = {double}]")
    public void делениеЦелых(int valA, int valB, double result) {
        assertEquals(result, new Calculator().division(valA, valB), 0.000000000000000001);
    }

}
