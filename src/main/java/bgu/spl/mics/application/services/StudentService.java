package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.Messages.*;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.FileToOut;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.concurrent.TimeUnit;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    Student myStudent;
    Future<Model> currFuture=null;
//    int timeForTick;
//    int duration;
//    int nextConfo;
//    int currtime;
//    boolean waitingForConference;
    private boolean wasInit=false;
    public StudentService(String _name){
        super(_name);
    }
    public void setStudent(Student _student){
        myStudent=_student;
    }
    private void sendNextEvent(){
        if(currFuture.get().getWasPublished()){
            Model modelToSend= myStudent.getNextModel();
            if(modelToSend != null)
                currFuture=sendEvent(new TrainEvent(modelToSend));
            else
                currFuture = null;
        }
        else{

            switch (currFuture.get().getStatus()) {
                case PreTrained:
                    currFuture = sendEvent(new TrainEvent(currFuture.get()));
                    break;
                case Trained:
                    Model trainedModel = currFuture.get();
                    currFuture = sendEvent(new TestEvent(trainedModel));
                    break;
                case Tested:
                    Model testedModel = currFuture.get();
                    myStudent.addTestedModel(testedModel);
                    if (currFuture.get().positiveResult()) {
                        currFuture = sendEvent(new PublishEvent(testedModel));
                    } else {
                        Model newModelToSend = myStudent.getNextModel();
                        if (newModelToSend != null)
                            currFuture = sendEvent(new TrainEvent(newModelToSend));
                        else
                            currFuture = null;
                    }
                    break;
            }
        }
    }
    protected void initialize() {
        subscribeBroadcast(ModelFinishedBroadcast.class, new FinishedModelCallback());
        subscribeBroadcast(PublishConferenceBroadcast.class, new PublicationsCallback());
        subscribeBroadcast(terminateBroadcast.class, new terminateCallback());
        if (currFuture == null) {
            Model firstModel = myStudent.getNextModel();
            if (firstModel != null)
                currFuture = sendEvent(new TrainEvent(firstModel));
        }
        wasInit = true;
    }
    public boolean isInit(){return wasInit;    }
    private class FinishedModelCallback implements Callback<ModelFinishedBroadcast>{
        public void call(ModelFinishedBroadcast b){
//            System.out.println(myStudent.getName()+" knows a model has finished");
            if(currFuture != null && currFuture.isDone()) {
                sendNextEvent();
            }
        }
    }
    private class PublicationsCallback implements Callback<PublishConferenceBroadcast>{
        public void call(PublishConferenceBroadcast b){
            myStudent.addPublications(b.getInfo());
        }
    }
    private class terminateCallback implements Callback<terminateBroadcast> {
        public void call(terminateBroadcast b) {
            FileToOut outputFile = FileToOut.getInstance();
//            Model[] modelsToOut = new Model[myStudent.getTestedModels().size()];
//            int i = 0;
//            for (Model modelToReturn : myStudent.getTestedModels()) {
//                modelsToOut[i] = modelToReturn;
//                i++;
//            }
            outputFile.modelsToOut(myStudent);//, modelsToOut);
            terminate();
        }
    }
}
