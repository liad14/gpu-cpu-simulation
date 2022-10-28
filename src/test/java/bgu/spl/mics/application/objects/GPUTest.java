package bgu.spl.mics.application.objects;
import bgu.spl.mics.Callback;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class GPUTest {
    private GPU g1;
    private GPU g2;
    private Data dTxt;
    private Data dIm;
    private Data dTab;
    private Model mTxt;
    private Model mIm;
    private Model mTab;
    private Callback<Integer> callback=null;
    private CPU c1;
    private Cluster cluster=Cluster.getInstance();

    @Before
    public void setUp()  {
        g1=new GPU(GPU.Type.RTX3090);//deleted the callback from constructor
        g2=new GPU(GPU.Type.GTX1080);//deleted the cluster & messageBus from constructor
        Student s1=new Student("stud1","CS", Student.Degree.PhD);
        Student s2=new Student("stud1","CS", Student.Degree.PhD);
        Student s3=new Student("stud1","CS", Student.Degree.PhD);
        mTxt=new Model("txtMod", Data.Type.Text,4*1000,s1);
        mIm=new Model("ImMod", Data.Type.Images,2*1000,s2);
        mTab=new Model("TabMod", Data.Type.Tabular,3*1000,s3);
        c1=new CPU(32);

    }

    @After
    public void tearDown()  {
        g1=null;
        g2=null;
        Student s1=null;
        Student s2=null;
        Student s3=null;
        mTxt=null;
        mIm=null;
        mTab=null;
        c1=null;
    }


    @Test
    public void train() {
        g1.addNewModel(mTab);
        g1.setCurrModelForTest(mTab);
        assertEquals("the unprocessed data was added to the unprocessed queue",3,g1.getNumberOfUnprocessedBatches());
        while(!g1.getUnprocessedListForTest().isEmpty()) {
            g1.addProcessedData(g1.getUnprocessedListForTest().poll());
        }
        g1.train(g1.getProcessedListForTest().poll());
        assertEquals("the processed data was reduced by one when we started training",2,g1.getNumberOfProcessedBatches());
        g1.incrementTick();
        assertEquals("the processed data was reduced by one when we started a new batch, and RXT3090 starts a new batch after 1 tick",g1.getNumberOfProcessedBatches(),1);

    }

    @Test
    public void incrementTick() {
        assertEquals("the GPU tick needs to be initialized at 0:",g1.getCurTick(),0);
        g1.incrementTick();
        assertEquals("the GPU total tick doesn't need to be increased if it isn't working:",g1.getCurTick(),0);
        List<DataBatch> batches=g1.makeBatches(mIm.getData(),mIm);
        g1.setCurrModelForTest(mIm);
        g1.addProcessedData(batches.remove(0));
        g1.train(g1.getProcessedListForTest().poll());
        g1.incrementTick();
        assertEquals("the training started and the GPU ticks needs to be increased: ",1,g1.getCurTick());
        g1.incrementTick();
        assertEquals("there are no processedDataBatches so the GPU ticks don't need to be increased: ",1,g1.getCurTick());
        g1.addProcessedData(batches.remove(0));
        g1.train(g1.getProcessedListForTest().poll());
        g1.incrementTick();
        assertEquals(" the training continued and the GPU ticks needs to  increase, updated to the last tick: ",2,g1.getCurTick());
    }

}