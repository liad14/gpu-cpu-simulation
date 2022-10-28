package bgu.spl.mics.micros;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.Messages.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.CPU;

public class CPUMicro extends MicroService {
    MessageBusImpl mBus;
    CPU myCPU;

    public CPUMicro(String name, CPU _cpu) {
        super(name);
        myCPU = _cpu;
        mBus = MessageBusImpl.getInstance();

    }

    public void initialize() {
        subscribeBroadcast(TickBroadcast.class, new TickCallback());
    }
    public CPU getCPU(){
        return myCPU;
    }


    private class TickCallback implements Callback<TickBroadcast> {
        public void call(TickBroadcast b){
            myCPU.incrementTick();
        }
    }
}