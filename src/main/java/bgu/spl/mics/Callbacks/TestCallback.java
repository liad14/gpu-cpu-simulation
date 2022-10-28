//package bgu.spl.mics.Callbacks;
//
//import bgu.spl.mics.Callback;
//import bgu.spl.mics.Messages.TestEvent;
//import bgu.spl.mics.application.objects.GPU;
//import bgu.spl.mics.micros.GPUMicro;
//
//public class TestCallback<Model> implements Callback {
//    private GPUMicro myMicro;
//    private GPU myGPU;
//    private TestEvent calledEvent;
//    public TestCallback(TestEvent e, GPUMicro _micro){
//        myMicro=_micro;
//        myGPU=_micro.getMyGPU();
//        calledEvent=e;
//    }
//    @Override
//    public void call(Model m){
//        myGPU.test(m);
//    }
//
//
//}
