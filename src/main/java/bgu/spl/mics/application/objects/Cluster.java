package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
	class compareQueuse implements Comparator<ConcurrentLinkedQueue>{
		public int compare(ConcurrentLinkedQueue lhs, ConcurrentLinkedQueue rhs){
			int lhsS=Q_CPU_map.get(lhs).getTimeToIdle();
			int rhsS=Q_CPU_map.get(rhs).getTimeToIdle();
			return lhsS < rhsS ? -1 : lhsS==rhsS ? 0 : 1;
		}
	}
	class compareGPU implements Comparator<GPU>{
		public int compare(GPU lhs, GPU rhs){
			int lhsS=lhs.getNumberOfProcessedBatches();
			int rhsS=rhs.getNumberOfProcessedBatches();
			return lhsS < rhsS ? -1 : lhsS==rhsS ? 0 : 1;
		}
	}

	ConcurrentHashMap<Model,GPU> model_gpu;//keeping track of witch batch came from witch GPU
	List<CPU> CPUS;
	List<GPU> GPUS;
	ConcurrentLinkedQueue<String> finishedModelsNames;
	AtomicInteger processedBatches;
	Comparator compareQueuse;
	Comparator compareGPU;
	Queue<ConcurrentLinkedQueue> CPUSHeap;
	ConcurrentHashMap<ConcurrentLinkedQueue<DataBatch>,CPU> Q_CPU_map;
	ConcurrentHashMap<CPU,ConcurrentLinkedQueue<DataBatch>> CPU_Q_map;


	private static class ClusterHolder{
		private static Cluster instance= new Cluster();
		}

	/**
     * Retrieves the single instance of this class.
     */
	private Cluster(){
		CPUS=Collections.synchronizedList(new LinkedList<CPU>());
		GPUS=Collections.synchronizedList(new LinkedList<GPU>());
		processedBatches=new AtomicInteger(0);
		compareQueuse=new compareQueuse();
		compareGPU=new compareGPU();
		CPUSHeap= new PriorityBlockingQueue(1,compareQueuse);
		Q_CPU_map=new ConcurrentHashMap<>();
		CPU_Q_map=new ConcurrentHashMap<>();
		model_gpu=new ConcurrentHashMap<>();
		finishedModelsNames = new ConcurrentLinkedQueue<>();

	}
	public static Cluster getInstance() {
		return ClusterHolder.instance;
	}

	public void registerCPU(CPU c){
		CPUS.add(c);
		ConcurrentLinkedQueue<DataBatch> cpuQ =new ConcurrentLinkedQueue<>();
		CPU_Q_map.put(c,cpuQ);
		Q_CPU_map.put(cpuQ,c);
		CPUSHeap.add(cpuQ);
	}
	public void registerGPU(GPU g){
		GPUS.add(g);
//		GPUSHeap.add(g);
	}

	public List<CPU> getCPUS() {
		return CPUS;
	}

	public List<GPU> getGPUS() {
		return GPUS;
	}

	public void sendToCPU(List<DataBatch> unprocessed, GPU modelsGPU) {
		if (model_gpu.get(unprocessed.get(0).getModel()) == null)
			model_gpu.put(unprocessed.get(0).getModel(), modelsGPU);
		ConcurrentLinkedQueue<DataBatch> tmpList = null;
		while (tmpList == null)
			tmpList = CPUSHeap.poll();
		int addedTime = 0;
		for (DataBatch batch : unprocessed) {
			tmpList.add(batch);
			addedTime = addedTime + batch.getData().getProcessTime();
			Q_CPU_map.get(tmpList).addTimeToIdle(addedTime);
		}
		CPUSHeap.add(tmpList);
	}
	public void finishedProcessing(DataBatch finishedBatch){
		processedBatches.getAndIncrement();
		Model processedModel = finishedBatch.getModel();
		model_gpu.get(processedModel) .addProcessedData(finishedBatch);
		if(finishedBatch.getData().isAllProcessed()) {
			model_gpu.remove(processedModel);
		}
	}
	public int getProcessedBatches() {
		return processedBatches.get();
	}

	public DataBatch requestBatchToProcess(CPU c){
		if(CPU_Q_map.get(c).isEmpty())
			return null;
		return CPU_Q_map.get(c).poll();
	}
	public void addFinishedModel(String modName){
		finishedModelsNames.add(modName);
	}

	public class stats{
		private List<String> names;
		private AtomicInteger GPUTime=new AtomicInteger(0);
		private AtomicInteger CPUTime=new AtomicInteger(0);
		public stats(List<CPU> CPUS,List<GPU> GPUS){
			for(GPU g:GPUS){
				GPUTime.compareAndSet(GPUTime.get(),GPUTime.get() + g.getCurTick());
				for(String modelName:g.getModelsName())
					names.add(modelName);
			}
			for(CPU c:CPUS){
				CPUTime.compareAndSet(CPUTime.get(),CPUTime.get() + c.getCurrTick());
			}


		}
		public int getGPUTime(){
			return GPUTime.get();
		}
		public int getCPUTime(){
			return CPUTime.get();
		}
		public int getProcessedBatches(){
			return Cluster.this.processedBatches.get();
		}
		public ConcurrentLinkedQueue<String> getNames(){
			return finishedModelsNames;
		}
	}
	public stats getStats(){
		return new stats(CPUS,GPUS);
	}

}
