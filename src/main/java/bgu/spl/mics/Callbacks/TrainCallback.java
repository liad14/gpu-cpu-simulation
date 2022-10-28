//package bgu.spl.mics.Callbacks;
//
//import bgu.spl.mics.Callback;
//import bgu.spl.mics.Messages.TrainEvent;
//import bgu.spl.mics.application.objects.DataBatch;
//import bgu.spl.mics.application.objects.GPU;
//import bgu.spl.mics.micros.GPUMicro;
//
//import java.util.List;
//
//public class TrainCallback<Model> implements Callback<Model> {
//    private GPUMicro myMicro;
//    private GPU myGPU;
//    private TrainEvent calledEvent;
//    public TrainCallback(TrainEvent e, GPUMicro _micro){
//        myMicro=_micro;
//        myGPU=_micro.getMyGPU();
//        calledEvent=e;
//    }
//    @Override
//    public void call(Model c){
//        List<DataBatch> myBatches=myGPU.makeBatches(calledEvent.getModel().getData());
//        for(DataBatch batch:myBatches){
//            myGPU.addUnprocessedData(batch);
//        }
//    }
//}