package bgu.spl.mics.Messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class PublishEvent implements Event<Model> {
    Model myModel;
    public PublishEvent(Model m){
        myModel=m;
    }
    public Model getModel(){
        return myModel;
    }
}
