package bgu.spl.mics;
import org.junit.After;
import org.junit.Before;
import bgu.spl.mics.Future;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class FutureTest {
    static Future<Integer> fu;
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }
    @Test
    public void get() throws InterruptedException {
        AtomicBoolean getIsBlocking= new AtomicBoolean(true);
        Future<Integer> f=new Future<Integer>();
        Thread t1=new Thread(()->{
            f.get();
            getIsBlocking.set(false);
        });
        t1.start();
        assertTrue("the get function is not blocking", getIsBlocking.get());
        Integer res=19;
        f.resolve(res);
        t1.join();
        assertFalse("the get function did not release the block even after the result was resolved", getIsBlocking.get());
    }

    @Test
    public void resolve() {
        Future<Integer> f=new Future<Integer>();
        Integer res=19;
        f.resolve(res);
        boolean equals = res==f.get();
        assertTrue("the resolve did not insert the right element",equals);
    }

    @Test
    public void isDone() {
        Future<Integer> f=new Future<Integer>();
        Integer res=19;
        f.resolve(res);
        assertTrue("the isDone did not return true",f.isDone());
    }

    @Test
    public void testGet() throws InterruptedException {
        AtomicBoolean getReturnsNull= new AtomicBoolean(true);
        Future<Integer> f=new Future<Integer>();
        TimeUnit unit = TimeUnit.MILLISECONDS;
        Thread t1=new Thread(()->{
            Integer i= f.get(1000,unit);
            getReturnsNull.set(i==null);
        });
        t1.start();
        assertTrue("get(timeout,units) is not blocking at all", getReturnsNull.get());
        t1.join();
        assertTrue("get(timeout,units) does not retrun null the block ", getReturnsNull.get());
        getReturnsNull.set(true);
        Thread t2=new Thread(()->{
            Integer i= f.get(1000,unit);
            getReturnsNull.set(false);
        });
        t2.start();
        Integer res=19;
        f.resolve(res);
        t2.join();
        assertFalse("get(timeout,units) does not release the block ", getReturnsNull.get());

    }

}