package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;
import bgu.spl.mics.Messages.*;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.FileToOut;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    int tick;
    ConfrenceInformation myConfo;
    private boolean wasInit=false;
    private ConcurrentHashMap<Model,Event> myHashMap;
    public ConferenceService(String name) {
        super(name);
        tick=0;
        myHashMap=new ConcurrentHashMap<>();
    }
    public void setConference(ConfrenceInformation c){
        myConfo=c;
    }
    public boolean isInit(){return wasInit;    }
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class , new TickCallback());
        subscribeEvent(PublishEvent.class , new PublishCallback());
        subscribeBroadcast(terminateBroadcast.class , new terminateCallback());
        wasInit=true;

    }
    private class TickCallback implements Callback<TickBroadcast> {
        public void call(TickBroadcast b) {
            tick++;
            if (myConfo.getDate() == tick) {
                sendBroadcast(new PublishConferenceBroadcast(myConfo));
                for(Model m:myConfo.getPublications()) {
                    m.setWasPublished();
                    complete(myHashMap.remove(m), m);
                }
                FileToOut outputFile = FileToOut.getInstance();
                outputFile.conferenceToOut(myConfo);
                sendBroadcast(new ModelFinishedBroadcast());
                System.out.println(myConfo.getName() + "this conference was terminated");

                terminate();
            }
        }
    }
    private class terminateCallback implements Callback<terminateBroadcast> {
        public void call(terminateBroadcast b){
            System.out.println(myConfo.getName() + "this conference was terminated");
            FileToOut outputFile = FileToOut.getInstance();
            outputFile.conferenceToOut(myConfo);
            terminate();
            }
        }
    private class PublishCallback implements Callback<PublishEvent> {
        public void call(PublishEvent p){
            myConfo.addModel(p.getModel());
            myHashMap.put(p.getModel(),p);
        }
    }
}
