package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private AtomicInteger GPUprocessed;
    private AtomicInteger CPUprocessed;
    final private int size;
    public Data(Type _type,int _size){
        type=_type;
        GPUprocessed= new AtomicInteger(0);
        CPUprocessed= new AtomicInteger(0);
        size=_size;
    }
    public boolean isAllProcessed(){
        return CPUprocessed.get()==size;
    }
    public boolean isAllTrained(){
        return GPUprocessed.get() * 1000 == size;
    }
    public int getSize(){
        return size;
    }
    public void incramentProcessed(){
        CPUprocessed.incrementAndGet();
    }
    public void incramentTrained(){
        GPUprocessed.incrementAndGet();
    }
    public int getProcessedUnits(){return CPUprocessed.get();}
    public int getProcessTime(){
        switch (type){
            case Images:
                return 4;
            case Text:
                return 2;
            default:
                return 1;
        }
    }
    public String getType() { return type.toString(); }
}
