package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.Messages.*;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    GPU myGPU;
    ConcurrentHashMap<Model, Event> mod_mess_map;
    private ConcurrentLinkedQueue<Model> modelsToTest;
    private boolean wasInit=false;

    public GPUService(String name) {
        super(name);
        mod_mess_map=new ConcurrentHashMap<>();
        modelsToTest=new ConcurrentLinkedQueue<>();
    }

    protected void initialize() {
        subscribeEvent(TrainEvent.class, new TrainCallback());
        subscribeEvent(TestEvent.class,new TestCallback());
        subscribeBroadcast(TickBroadcast.class,new GPUTickCallback());
        subscribeBroadcast(terminateBroadcast.class , new terminateCallback());
        wasInit=true;
    }
    public void setMyGPU(GPU gpu){
     myGPU  = gpu;
    }
    public GPU getMyGPU() {
        return myGPU;
    }
    public void completed(Model m){
        complete(mod_mess_map.remove(m),m);
        sendBroadcast(new ModelFinishedBroadcast());

    }
    public boolean isInit(){return wasInit;    }
    private class TrainCallback implements Callback<TrainEvent> {
        @Override
        public void call(TrainEvent event) {
                myGPU.addNewModel(event.getModel());
                mod_mess_map.put(event.getModel(), event);
            }
    }
    private class TestCallback implements Callback<TestEvent> {
        @Override
        public void call(TestEvent event) {
            modelsToTest.add(event.getModel());
            mod_mess_map.put(event.getModel(), event);
        }

    }
    private class GPUTickCallback implements Callback<TickBroadcast>{

        public void call(TickBroadcast b){
                Model finishedTraining = myGPU.incrementTick();
                if (finishedTraining != null) {
                    completed(myGPU.getModel());
                    while (!modelsToTest.isEmpty()) {
                        completed( myGPU.test(modelsToTest.poll()));
                    }
                } else if (!(myGPU.getAmITrainingNow())) {
                    while (!modelsToTest.isEmpty()) {
                        completed( myGPU.test(modelsToTest.poll()));
                    }
                }
        }
    }
    private class terminateCallback implements Callback<terminateBroadcast> {
        public void call(terminateBroadcast b){
            terminate();
        }
    }
}