package bgu.spl.mics.application.objects;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.services.CPUService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    final private Integer cores;
    private List<DataBatch> dataCollection;
    private DataBatch data;
    private Cluster cluster;
    private AtomicInteger currTick;
    private AtomicInteger processTicksLeft;
    private AtomicInteger timeToIdle;
    /**
     * constructor of the CPU, gets the number of cores to initialize and the cluster it will comunicate with
     * @pre nCores>0
     * @pre clust!=null
     */
    public CPU(int nCores){
        timeToIdle=new AtomicInteger(0);
        processTicksLeft=new AtomicInteger(0);
        currTick=new AtomicInteger(0);
        cores=nCores;
        cluster=Cluster.getInstance();
        cluster.registerCPU(this);
        data=null;
    }

    public void incrementTick(){
        if(data == null){
            data=getNextBatch();
            resetProcessTick();

        }
        else{
            currTick.incrementAndGet();
            timeToIdle.decrementAndGet();
            if(processTicksLeft.decrementAndGet() == 0){
                data.getData().incramentProcessed();
                cluster.finishedProcessing(data);
                data=getNextBatch();
                resetProcessTick();
            }
        }
    }

    /**
     * function that gets the total amount of ticks this CPU has processed
     */
    public int getCurrTick(){
        return currTick.get();
    }

    /**
     * return the ticks left for processing the current batch
     * @return
     */
    public int getProcessTicksLeft(){
        return processTicksLeft.get();
    }

    /**
     * returns this CPU's microService
     * @return
     */
    private void resetProcessTick(){
        if(data != null) {
            int newProcessTick = (32 / cores) * data.getData().getProcessTime();
            processTicksLeft.set(newProcessTick);
        }
    }

    /**
     * gets a DataBatch and returns if after a while (depending on its type),
     * the returned Batch will be marked as "processed"
     * @param batch
     * @return
     */
    public DataBatch process(DataBatch batch){
        return batch;
    }

    /**
     * returns the cluster field
     * @return
     */
    public Cluster getCluster(){
        return cluster;
    }

    /**
     * returns the data the CPU is currently working on
     * @return
     */
    public DataBatch getData(){
        return data;
    }

    /**
     * returns the amount of batches in line for this cpu
     * so maybe the cluster could manage to witch CPU to give the next batch
     * @return
     */
    public int getNumberOfBatchesWaiting(){
        return dataCollection.size();
    }
    public void addBatchList(List<DataBatch> addedBatch){
    }
    public DataBatch getNextBatch(){
        return cluster.requestBatchToProcess(this);
    }
    public int getTimeToIdle(){return timeToIdle.get();}
    public void addTimeToIdle(int time){
        int timeToAdd=(32/cores)*time;
        timeToIdle.compareAndSet(timeToIdle.get(),timeToIdle.get()+timeToAdd);
    }
}
