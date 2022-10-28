package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private static class MessageBusHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}
	//-------------------Fields--------------------------------------------------
	ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> microServiceToQueue; //TODO as array to fast find and remove.
	ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<LinkedBlockingQueue<Message>>> messagesToQueue; // insert each messege (event or broadcast) to the right queue of queue of the microServices that are subscribed to
	ConcurrentHashMap<MicroService, LinkedBlockingQueue<Class<? extends Message>>> microServiceToSubscribes;
	ConcurrentHashMap<Event, Future> eventToFuture;

	//------------------Constructor----------------------
	private <T> MessageBusImpl() {
		microServiceToQueue = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		messagesToQueue = new ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<LinkedBlockingQueue<Message>>>();
		microServiceToSubscribes = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Class<? extends Message>>>();
		eventToFuture = new ConcurrentHashMap<Event, Future>();
	}
	public static MessageBusImpl getInstance(){
		return MessageBusHolder.instance;
	}
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (messagesToQueue) {
			if (messagesToQueue.isEmpty() || messagesToQueue.get(type) == null) {
				LinkedBlockingQueue<LinkedBlockingQueue<Message>> newQueue = new LinkedBlockingQueue<LinkedBlockingQueue<Message>>();
				messagesToQueue.put(type, newQueue);
			}
			LinkedBlockingQueue<Message> microsQ = microServiceToQueue.get(m);
			if (microsQ != null) {
				messagesToQueue.get(type).add(microsQ);
				microServiceToSubscribes.get(m).add(type);
			}
		}
	}
		@Override
		public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
			synchronized (messagesToQueue) {
				if (messagesToQueue.isEmpty() || messagesToQueue.get(type) == null) {
					LinkedBlockingQueue<LinkedBlockingQueue<Message>> newQueue = new LinkedBlockingQueue<LinkedBlockingQueue<Message>>();
					messagesToQueue.put(type, newQueue);
				}
				LinkedBlockingQueue<LinkedBlockingQueue<Message>> thisTypeQueue = messagesToQueue.get(type);
				thisTypeQueue.add(microServiceToQueue.get(m));
				microServiceToSubscribes.get(m).add(type);

			}
		}

		@Override
		public <T> void complete(Event<T> e, T result) {
				Future f= eventToFuture.remove(e);
				if(f!=null)
					f.resolve(result); //TODO resolve does notifyAll to the student, or line below
		}

		@Override
		public void sendBroadcast(Broadcast b) {
			synchronized (messagesToQueue) {
				if(messagesToQueue.get(b.getClass()) != null) {
					Iterator<LinkedBlockingQueue<Message>> queuesIter = messagesToQueue.get(b.getClass()).iterator();
					while (queuesIter.hasNext()) {
						LinkedBlockingQueue<Message> curQ = queuesIter.next(); //TODO check didn't miss the first
						curQ.add(b);
					}
				}
			}
		}

		@Override
		public <T> Future<T> sendEvent(Event<T> e) { //TODO add t futures hashmap
			synchronized (messagesToQueue) {
				if (messagesToQueue.get(e.getClass()) != null) {
					LinkedBlockingQueue<Message> addToQueue = messagesToQueue.get(e.getClass()).poll();
					if (addToQueue != null) {
						addToQueue.add(e);
						messagesToQueue.get(e.getClass()).add(addToQueue);
						Future<T> newFuture = new Future<>();
						eventToFuture.put(e, newFuture);
						return newFuture;
					}
				}
			}
			return  null;
		}

		@Override
		public void register(MicroService m) {
			LinkedBlockingQueue<Message> newQueue = new LinkedBlockingQueue<Message>();
			synchronized (microServiceToQueue) {
				microServiceToQueue.put(m, newQueue);
				LinkedBlockingQueue<Class<? extends Message>> mySubscibedMessages = new LinkedBlockingQueue<>();
				microServiceToSubscribes.put(m, mySubscibedMessages);
			}
		}

		@Override
		public void unregister(MicroService m) {
			LinkedBlockingQueue<Message> queueToDelete = microServiceToQueue.remove(m);
			synchronized (messagesToQueue){
				if (queueToDelete != null) {
					LinkedBlockingQueue<Class<? extends Message>> toDeleteSubscribesList = microServiceToSubscribes.remove(m);
					if (toDeleteSubscribesList != null) {
						Iterator<Class<? extends Message>> subscribeIter = toDeleteSubscribesList.iterator();
						while (subscribeIter.hasNext()) {
							Class<? extends Message> subscribeToDelete = subscribeIter.next();
							messagesToQueue.get(subscribeToDelete).remove(queueToDelete);
							toDeleteSubscribesList.remove(subscribeToDelete);
						}
					}
				}
			}
		}
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException ,IllegalStateException{//or wait() for student?
		if(microServiceToQueue.get(m) != null)
			return microServiceToQueue.get(m).take();
		return null;
	}
	public void clearForTests() {
		microServiceToQueue = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		messagesToQueue = new ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<LinkedBlockingQueue<Message>>>();
		microServiceToSubscribes = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Class<? extends Message>>>();
		eventToFuture = new ConcurrentHashMap<Event, Future>();
	}
	public boolean isSubscribedToSomething(MicroService m) {
		return !microServiceToSubscribes.get(m).isEmpty();
	}
	public boolean aMicroIsSubscribedToMessage(Class<? extends Message> mess) {
		return messagesToQueue.get(mess)!=null;
	}
	public Future getFutureOfEvent(Event e){
		return eventToFuture.get(e);
	}
	public boolean isRegistered(MicroService m){
		return microServiceToQueue.get(m) != null;
	}
	public int getNumberOfSubscribers(Class<? extends Message> mess){
		return messagesToQueue.get(mess).size();
	}


}
