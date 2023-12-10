package methods.evaluationControl;

import interfaces.IEvaluator;

import java.util.ArrayList;

public class EvaluationResult {
    private IEvaluator method;
    private ArrayList<Double> values;

    public EvaluationResult(IEvaluator method){
        this.method = method;
        values = new ArrayList<>();
    }

    public void setValues(double value){
        this.values.add(value);
    }

    public IEvaluator getMethod(){
        return this.method;
    }
    public ArrayList<Double> getValues(){
        return this.values;
    }
}
