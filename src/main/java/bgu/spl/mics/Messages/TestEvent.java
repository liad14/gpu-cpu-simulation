package bgu.spl.mics.Messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class TestEvent implements Event<Model> {
    private Model modelToTest;
    public TestEvent(Model _model){
        modelToTest=_model;
    }
    public Model getModel(){
        return modelToTest;
    }
    public String toString(){return "test model 1";}
}
