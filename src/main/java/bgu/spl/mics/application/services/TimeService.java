package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Callback;
import bgu.spl.mics.Messages.TickBroadcast;
import bgu.spl.mics.MicroService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Messages.terminateBroadcast;
import bgu.spl.mics.application.objects.Cluster;


/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{
	Timer myTimer;
	int currentTime;
	int delay;
	int period;
	Cluster clust;
	Cluster.stats s;
	public TimeService(int _delay, int terminate) {
		super("TimeService");
		myTimer = new Timer(true);
		currentTime = 1;
		delay = _delay;
		period = terminate * delay;
		clust = Cluster.getInstance();
		s = clust.getStats();
	}

	@Override
	protected void initialize() {
		myTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				sendBroadcast(new TickBroadcast(currentTime));
				if(currentTime>=period) {
					System.out.println("the delay is: "+delay);
					System.out.println("the duration is: "+ period);
					System.out.println(" the total CPU time is: "+ s.getCPUTime());
					System.out.println("the total GPU time is: "+s.getGPUTime());
					System.out.println("the total Batches processed: "+s.getProcessedBatches());
					ConcurrentLinkedQueue<String> names=s.getNames();
					for(String s:names)
						System.out.print(" "+s);
					sendBroadcast(new terminateBroadcast());
					this.cancel();
				}
			}
		},1,delay);
		subscribeBroadcast(TickBroadcast.class, new TimeTickCallback());
		subscribeBroadcast(terminateBroadcast.class, new terminateCallback());

		
	}
	private class TimeTickCallback implements Callback<TickBroadcast> {

		public void call(TickBroadcast b){
			currentTime=currentTime+delay;
		}
	}
	private class terminateCallback implements Callback<terminateBroadcast> {
		public void call(terminateBroadcast b){
			terminate();
		}
	}


}
