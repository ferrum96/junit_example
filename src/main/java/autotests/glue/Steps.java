package autotests.glue;

import autotests.Calculator;
import cucumber.api.java.ru.Когда;

import static org.junit.Assert.assertEquals;

public class Steps {

    @Когда("значение {double} + значение {double} = {double}")
    public void сложение(double valA, double valB, double result){
        assertEquals(result, new Calculator().addition(valA,valB), 0.00000001);
    }

    @Когда("значение {double} - значение {double} = {double}")
    public void вычитание(double valA, double valB, double result){
        assertEquals(result, new Calculator().subtraction(valA,valB), 0.00000001);
    }

    @Когда("значение {double} * на значение {double} = {double}")
    public void умножение(double valA, double valB, double result){
        assertEquals(result, new Calculator().multiplication(valA,valB), 0.00000001);
    }

    @Когда("значение {double} делить на значение {double} = {double}")
    public void деление(double valA, double valB, double result){
        assertEquals(result, new Calculator().division(valA,valB), 0.00000001);
    }

}
