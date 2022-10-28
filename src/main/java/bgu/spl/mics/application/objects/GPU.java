package bgu.spl.mics.application.objects;
import bgu.spl.mics.*;
import bgu.spl.mics.application.services.GPUService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}


    private AtomicInteger capacity;
    private Type type;
    private Cluster cluster;
    private GPUService GPUmicro;
    private Model currModel;
    private List<Model> allModels;
    private AtomicInteger curTick=new AtomicInteger(0);
    private AtomicInteger currBatchTickLeft= new AtomicInteger(0);
    private ConcurrentLinkedQueue<DataBatch> unprocessedBatches;
    private ConcurrentLinkedQueue<DataBatch> processedBatches;
    private List<String> trainedModelsName;
    AtomicBoolean working=new AtomicBoolean(false);
    private final int initCapacity;
    private boolean amITrainingNow;

    /**
     * the constructor of this class gets the messageBus and cluster to communicate with
     * and the type of this GPU
     * @param thisType
     */
    public GPU(Type thisType){
        type=thisType;
        capacity=new AtomicInteger(type==Type.RTX3090 ? 32 : type==Type.RTX2080 ? 16 : 8);
        initCapacity=capacity.get();
        cluster=Cluster.getInstance();
        unprocessedBatches=new ConcurrentLinkedQueue<>();
        processedBatches=new ConcurrentLinkedQueue<>();
        allModels=Collections.synchronizedList(new LinkedList<>());
        cluster.registerGPU(this);
        trainedModelsName=Collections.synchronizedList(new LinkedList<>());
        amITrainingNow = false;
    }
//
//    /**
//     * run() is the main method in the CPU, this method will get the Data batches
//     * from the cluster and use the different methods of CPU.
//     * @pre this!=null
//     */
//    public void run(){}

    public boolean getAmITrainingNow() {
        return amITrainingNow;
    }

    public void setAmITrainingNow(boolean amITrainingNow) {
        this.amITrainingNow = amITrainingNow;
    }

    public Type getType(){
            return type;
        }

    /**
     * returns the model the GPU is currently training
     * @return
     */
    public Model getModel(){
            return currModel;
        }

    /**
     * returns the cluster that this GPU is communicating with
     * @return
     */
    public Cluster getCluster(){
            return cluster;
        }

//    /**
//     * returns the callBack that called this GPU's microService
//     * @return
//     */
//    public Callback getCallback(){
//        return callback;
//    }

    /**
     * the method where the training takes place
     * @pre !processedBatch.isEmpty();
     * @post if(incrementTick()) currBatchTickLeft--;
     * @post if(trainingModel.get(processedBatch.get(0))==null) this.finished();
     */
    public void train(DataBatch batchToTrain){
    switch (type) {
        case RTX3090:
            currBatchTickLeft.set(1);
            break;
        case RTX2080:
            currBatchTickLeft.set(2);
            break;
        default:
            currBatchTickLeft.set(4);
    }
    capacity.incrementAndGet();
    }

    /**
     * adding a new model to this GPU's disk, the Model contains unprocessed data
     * @param newModel
     * @post getAllModels.get(newModel)!=null
     */
    public void addNewModel(Model newModel) {
       // synchronized (this) {
            List<DataBatch> myBatches = makeBatches(newModel.getData(), newModel);
            for (DataBatch batch : myBatches) {
                addUnprocessedData(batch);
            }
           /* if (currModel != null) {
                if (currModel.getData().isAllTrained())
                    System.out.println(newModel.getName() + " batches are now part of myBatches in GPU and the currModel is: " + currModel.getName() + " and the number of processed data in the currModel is: " + currModel.getData().getProcessedUnits() + " and it is all trained");
                else
                    System.out.println(newModel.getName() + " batches are now part of myBatches in GPU and the currModel is: " + currModel.getName() + " and the number of processed data in the currModel is: " + currModel.getData().getProcessedUnits() + " it is nnoott traint");

            }
            allModels.add(newModel);
        }*/
    }


//    public void setMicro(GPUService m){
//        GPUmicro=m;
//    }
//    /**
//     * returns this GPU's MicroService
//     * @return
//     */
//    public MicroService getMicro() {
//        return GPUmicro;
//    }
//

    /**
     * adding the data returned from the CPU to a dataStructure
     */
    public void addProcessedData(DataBatch newBatch){
        processedBatches.add(newBatch);
    }

    public void addUnprocessedData(DataBatch newBatch){
        unprocessedBatches.add(newBatch);
    }

    /**
     * returns the capacity for processed data left
     * @return
     */
    public int getCapacity(){
            return capacity.get();
    }

    /**
     * takes the data and divides it to batches of size 1000
     * returns it as a list
     * @param modelData
     * @return
     * @post list.size()*1000==modelData.getSize()
     */
    public synchronized List<DataBatch> makeBatches(Data modelData,Model dataModel){
        List<DataBatch> newBatches=new LinkedList<>();
        for(int i=0; i < modelData.getSize(); i=i+1000){
            newBatches.add(new DataBatch(modelData,i,dataModel));
        }
        return newBatches;
    }


    /**
     * when a test event has been received, this method will get event and update its future result
     * using its microService with the messageBus
     * @pre m.getStatus==Trained
     * @post micro.sendresult(m.getResult);
     * @param m
     */
    public Model test(Model m){
        Student.Degree studentsDeg= m.getStudent().getDegree();
        boolean isPossitive;
        switch (studentsDeg){
            case PhD:
                if(Math.random()>0.6)
                    isPossitive=true;
                else
                    isPossitive=false;
                break;
            default:
                if(Math.random() > 0.8)
                    isPossitive=true;
                else
                    isPossitive=false;
        }
        if(isPossitive)
            m.setResult(Model.Result.GOOD);
        else
            m.setResult(Model.Result.BAD);
        m.setStatus(Model.Status.Tested);
        return m;
    }

    public List<Model> getAllModels(){
        return allModels;
    }
    public synchronized void finished(Model m){
        m.setStatus(Model.Status.Trained);
        amITrainingNow = false;
        cluster.addFinishedModel(m.getName());
    }
    /**
     * adds 1 to the currTick field if the GPU is currently training
     * @post if(currModel==null) currTick=currTick;
     *        else currTick++;
     *
     */
    public Model incrementTick(){
        synchronized (this){
            if(currBatchTickLeft.get() > 0) {
                curTick.incrementAndGet();
                if (currBatchTickLeft.decrementAndGet() == 0) {
                    currModel.getData().incramentTrained();
                    if (currModel.getData().isAllTrained()) {
                        finished(currModel);
                        return currModel;
                    } else if (!processedBatches.isEmpty()) {
                        train(processedBatches.poll());
                    }
                }
            }else if (!processedBatches.isEmpty()) {
                    train(processedBatches.poll());
                }
            else if (!unprocessedBatches.isEmpty() & !amITrainingNow) {
                    currModel=unprocessedBatches.peek().getModel();
                    currModel.setStatus(Model.Status.Training);
            }
            if((capacity.get() >= initCapacity/4) & (!unprocessedBatches.isEmpty()) &&  (unprocessedBatches.peek().getModel()==currModel))
                cluster.sendToCPU(sendNextBatchToProcess(),this);
            return null;
            }
        }


    /**
     * this method will send the next data batches to be processed.
     * it will only send the amount that the GPU can recive back.
     * the method will only send Batches from the same model
     * and only the model currently in training so the different batches wont get mixed
     * @post while(currmodel.get(UnprocessedBatches.get(0))!=null) outputList.add(UnprocessedBatches.remove(0))
     * @post return outputList;
     * @return
     */
    public List<DataBatch> sendNextBatchToProcess(){
        amITrainingNow=true;
        List<DataBatch> batchesTosend=Collections.synchronizedList(new LinkedList<>());
        while((capacity.get() > 0) & (!unprocessedBatches.isEmpty()) &&  (unprocessedBatches.peek().getModel()==currModel)){
            batchesTosend.add(unprocessedBatches.poll());
            capacity.decrementAndGet();
        }
        return batchesTosend;
    }
    public List<String> getModelsName(){return trainedModelsName;}
    public int getCurTick(){return curTick.get(); }
    public int getCurrBatchTickLeft(){ return currBatchTickLeft.get(); }
    public int getNumberOfUnprocessedBatches(){ return unprocessedBatches.size(); }
    public int getNumberOfProcessedBatches(){ return processedBatches.size(); }
    public void setCurrModelForTest(Model m){
        currModel=m;
    }
    public ConcurrentLinkedQueue<DataBatch> getUnprocessedListForTest(){return unprocessedBatches;}
    public ConcurrentLinkedQueue<DataBatch> getProcessedListForTest(){return processedBatches;}

}


