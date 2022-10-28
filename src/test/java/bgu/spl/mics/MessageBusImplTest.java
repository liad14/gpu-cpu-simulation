package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    static MessageBusImpl mBus;

    @Before
    public void setUp() throws Exception {
        mBus=MessageBusImpl.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        mBus.clearForTests();
    }

    @Test
    public void subscribeEvent() {
        testMicroService m1=new testMicroService("micro1");
        testMicroService m2=new testMicroService("micro2");
        mBus.register(m1);
        mBus.sendEvent(new testEvent1());
        assertFalse("micro should not be subscribed to anything",mBus.isSubscribedToSomething(m1));
        mBus.subscribeEvent(testEvent1.class,m1);
        mBus.sendEvent(new testEvent1());
        assertTrue("micro should be subscribed to something",mBus.isSubscribedToSomething(m1));
        assertNotNull("the micro should be subscribed to the specific Event: ",mBus.aMicroIsSubscribedToMessage(testEvent1.class));


    }

    @Test
    public void subscribeBroadcast() {
        testMicroService m1=new testMicroService("micro1");
        mBus.register(m1);
        mBus.sendBroadcast(new testBroadcast1());
        assertFalse("micro should not be subscribed to anything",mBus.isSubscribedToSomething(m1));
        mBus.subscribeBroadcast(testBroadcast1.class,m1);
        mBus.sendBroadcast(new testBroadcast1());
        assertTrue("micro should be subscribed to something",mBus.isSubscribedToSomething(m1));
        assertNotNull("the micro should be subscribed to the specific Broadcast: ",mBus.aMicroIsSubscribedToMessage(testBroadcast1.class));

    }

    @Test
    public void complete() {
        testMicroService m1=new testMicroService("micro 1");
        testEvent1 e1=new testEvent1();
        String result="the result";
        mBus.register(m1);
        mBus.subscribeEvent(testEvent1.class,m1);
        mBus.sendEvent(e1);
        assertNotNull("a future needs to be created",mBus.getFutureOfEvent(e1));
        assertFalse("the future was already done before complete() was called",mBus.getFutureOfEvent(e1).isDone());
        mBus.complete(e1,result);
        assertNull("the future is done" ,mBus.getFutureOfEvent(e1));
    }

    @Test
    public void sendBroadcast() {
        testMicroService m1=new testMicroService("micro 1");
        testMicroService m2=new testMicroService("micro 2");
        testMicroService m3=new testMicroService("micro 3");
        testMicroService m4=new testMicroService("micro 4");
        mBus.register(m1);
        mBus.register(m2);
        mBus.register(m3);
        mBus.register(m4);
        mBus.subscribeBroadcast(testBroadcast1.class,m1);
        mBus.subscribeBroadcast(testBroadcast1.class,m2);
        mBus.subscribeBroadcast(testBroadcast1.class,m3);
        mBus.subscribeEvent(testEvent1.class,m4);
        testBroadcast1 b1= new testBroadcast1();
        testEvent1 e1=new testEvent1();
        mBus.sendBroadcast(b1);
        mBus.sendEvent(e1);
        try {
            Message returnedb1=mBus.awaitMessage(m1);
            Message returnedb2=mBus.awaitMessage(m2);
            Message returnedb3=mBus.awaitMessage(m3);
            Message returnedb4=mBus.awaitMessage(m4);
            assertNotEquals("the broadcast was received to the unsubscribed micro4", returnedb4, b1);
            assertEquals("the broadcast was not received to the  subscribed micro2", returnedb2, b1);
            assertEquals("the broadcast was not received to the  subscribed micro3", returnedb3, b1);
            assertEquals("the broadcast was not received to the  subscribed micro1", returnedb1, b1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void sendEvent() throws InterruptedException {

        //check the round robin pattern:
        testMicroService m10=new testMicroService("micro 1");
        testMicroService m20=new testMicroService("micro 2");
        testMicroService m30=new testMicroService("micro 2");
        mBus.register(m10);
        mBus.register(m20);
        mBus.register(m30);
        mBus.subscribeEvent(testEvent1.class,m10);
        mBus.subscribeEvent(testEvent1.class,m20);
        mBus.subscribeEvent(testEvent1.class,m30);
        testEvent1 e10=new testEvent1();
        testEvent1 e20=new testEvent1();
        testEvent1 e30=new testEvent1();
        testEvent1 e40=new testEvent1();
        testEvent1 e50=new testEvent1();
        testEvent1 e60=new testEvent1();
        mBus.sendEvent(e10);
        mBus.sendEvent(e20);
        mBus.sendEvent(e30);
        Message e11=null;
        Message e22=null;
        Message e33=null;
        try {
             mBus.awaitMessage(m10);
        }catch(InterruptedException e){}
        try {
             mBus.awaitMessage(m20);
        }catch(InterruptedException e){}
        try {
             mBus.awaitMessage(m30);
        }catch(InterruptedException e){}
        mBus.sendEvent(e40);
        mBus.sendEvent(e50);
        mBus.sendEvent(e60);
        try {
             e33= mBus.awaitMessage(m30);
        }catch(InterruptedException e){}
        try {
             e22= mBus.awaitMessage(m20);
        }catch(InterruptedException e){}
        try {
             e11= mBus.awaitMessage(m10);
        }catch(InterruptedException e){}
        assertEquals("the round robin pattern does not work in m3",e33,e60);
        assertEquals("the round robin pattern does not work in m2",e22,e50);
        assertEquals("the round robin pattern does not work in m1",e11,e40);

    }

    @Test
    public void register() {
        testMicroService m1=new testMicroService("micro 1");
        testMicroService m2=new testMicroService("micro 2");
        mBus.register(m1);
        assertFalse("the wrong micro was registered",mBus.isRegistered(m2));
        assertTrue("the micro was not registered",mBus.isRegistered(m1));
    }

    @Test
    public void unregister() {
        testMicroService m1=new testMicroService("micro 1");
        testMicroService m2=new testMicroService("micro 1");
        mBus.register(m1);
        mBus.register(m2);
        testEvent1 e1=new testEvent1();
        testBroadcast1 b1= new testBroadcast1();
        mBus.subscribeEvent(e1.getClass(),m1);
        mBus.subscribeBroadcast(b1.getClass(),m1);
        mBus.subscribeEvent(e1.getClass(),m2);
        mBus.subscribeBroadcast(b1.getClass(),m2);
        assertEquals("both micros should be registered to the broadcast", 2,mBus.getNumberOfSubscribers(b1.getClass()));
        assertEquals("both micros should be registered to the event", 2,mBus.getNumberOfSubscribers(e1.getClass()));
        mBus.unregister(m1);
        assertFalse("the micro should not be still registered", mBus.isRegistered(m1));
        assertEquals("the micro m1 should not be still subscribed to broadcast b1, but m2 should", 1,mBus.getNumberOfSubscribers(b1.getClass()));
        assertEquals("the micro m2 should be subscribed to event e1 after m1 unregistered, but m2 should", 1,mBus.getNumberOfSubscribers(b1.getClass()));
    }

    @Test
    public void awaitMessage() {
        AtomicBoolean blocking_check= new AtomicBoolean(false);
        testMicroService m1=new testMicroService("micro 1");
        testMicroService m2=new testMicroService("micro 2");
        mBus.register(m1);
        mBus.register(m2);
        mBus.subscribeEvent(testEvent1.class,m1);
        testEvent1 e1=new testEvent1();
        Thread t1=new Thread(() ->{
        try {
            Message returned=mBus.awaitMessage(m1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        blocking_check.set(true);
       } );
        t1.start();
        assertFalse(" awaitMessage needs to be blocking:",blocking_check.get());
    }
}
class testMicroService extends MicroService{
    public testMicroService (String name) {
        super(name);
    }
    protected void initialize() {
        System.out.println(getName() + " is initialized");
    }
}
class testBroadcast1 implements Broadcast{}
class testBroadcast2 implements Broadcast{}
class testEvent1 implements Event<String>{}
class testEvent2 implements Event<Integer>{}
