package bgu.spl.mics.Messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class TrainEvent implements Event<Model> {
    private Model modelToTrain;
    public TrainEvent(Model _model){
        modelToTrain=_model;
    }
    public Model getModel(){
        return modelToTrain;
    }
}

