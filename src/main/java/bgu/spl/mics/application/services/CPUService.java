package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Messages.TickBroadcast;
import bgu.spl.mics.Messages.terminateBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.CPU;

/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    CPU myCPU;
    private boolean wasInit=false;

    public CPUService(String name) {
        super(name);

    }

    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, new TickCallback());
        subscribeBroadcast(terminateBroadcast.class , new terminateCallback());
        wasInit=true;
    }
    public boolean isInit(){return wasInit;    }
    public CPU getCPU(){
        return myCPU;
    }
    public void setCPU(CPU _cpu){
        myCPU=_cpu;
    }

    private class TickCallback implements Callback<TickBroadcast> {
        public void call(TickBroadcast b){
                myCPU.incrementTick();
        }
    }
    private class terminateCallback implements Callback<terminateBroadcast> {
        public void call(terminateBroadcast b){
            terminate();
        }
    }
}
