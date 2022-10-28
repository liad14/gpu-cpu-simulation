package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.objects.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class CPUTest {
    CPU c1;
    Model modTab;
    Model modIm;
    Student stud;
    Student stud2;
    DataBatch batchTab;
    DataBatch batchIm;
    GPU gpu;
    Cluster cluster=Cluster.getInstance();
    List<DataBatch> dataList;


    @Before
    public void setUp(){
        c1=new CPU(32);
        modTab=new Model("CPUtestMod", Data.Type.Tabular,5*1000,stud);
        modIm=new Model("CPUtestMod", Data.Type.Images,5*1000,stud2);
        batchIm=new DataBatch(modIm.getData(),0,modIm);
        batchTab=new DataBatch(modTab.getData(),0,modTab);
        gpu=new GPU(GPU.Type.RTX2080);
        dataList=new LinkedList<>();
        dataList.add(batchIm);
    }

    @After
    public void tearDown() {
        c1=null;
        modIm=null;
        modTab=null;
        batchIm=null;
        batchTab=null;
    }

    @Test
    public void incrementTick() {
        assertEquals("the CPU tick needs to be initialized at 0:",0,c1.getCurrTick());
        cluster.sendToCPU(dataList,gpu);
        c1.incrementTick();
        assertEquals("the CPU total tick doesn't need to be increased if it isn't working:",0,c1.getCurrTick());
        c1.incrementTick();
        assertEquals("the CPU total tick need to be increased if it is working, incrementTick gets a new batch from the cluster correctly:",c1.getCurrTick(),1);
        c1.incrementTick();
        assertEquals("the CPU total tick need to be increased if it is working:",c1.getCurrTick(),2);
        c1.incrementTick();
        assertEquals("the CPU total tick need to be increased if it is working:",c1.getCurrTick(),3);
        c1.incrementTick();
        assertEquals("the CPU total tick need to be increased if it is working :",c1.getCurrTick(),4);
        c1.incrementTick();
        assertEquals("the CPU total tick dosent need to be increased if it isn't working:",c1.getCurrTick(),4);

    }

}