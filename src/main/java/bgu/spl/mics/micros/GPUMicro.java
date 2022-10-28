package bgu.spl.mics.micros;

import bgu.spl.mics.*;
import bgu.spl.mics.Messages.TestEvent;
import bgu.spl.mics.Messages.TickBroadcast;
import bgu.spl.mics.Messages.TrainEvent;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.HashMap;
import java.util.List;

public class GPUMicro extends MicroService {
    GPU myGPU;
    MessageBusImpl myBus=MessageBusImpl.getInstance();
    HashMap<Model, Event> mod_mess_map;

    public GPUMicro(String name, GPU _gpu) {
        super(name);
        myGPU = _gpu;
    }

    protected void initialize() {
        super.subscribeEvent(TrainEvent.class, new TrainCallback());
        super.subscribeEvent(TestEvent.class,new TestCallback());
        super.subscribeBroadcast(TickBroadcast.class,new GPUTickCallback());
   }

    public GPU getMyGPU() {
        return myGPU;
    }
    public void completed(Model m){
        complete(mod_mess_map.get(m),m);
    }

    private class TrainCallback implements Callback<TrainEvent> {
        @Override
        public void call(TrainEvent event){
            myGPU.addNewModel(event.getModel());
            mod_mess_map.put(event.getModel(), event);
        }
    }

    private class TestCallback implements Callback<TestEvent> {

        @Override
        public void call(TestEvent event) {
            myGPU.test(event.getModel());
            mod_mess_map.put(event.getModel(), event);
        }

    }



    private class GPUTickCallback implements Callback<TickBroadcast>{

        public void call(TickBroadcast b){
            myGPU.incrementTick();
        }
    }
}
