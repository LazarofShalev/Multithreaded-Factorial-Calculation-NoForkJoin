
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FactorialCalculationNoForkJoin {

	final static int NUM = 20;

	public static void main(String[] args) {

		int np = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of Available Processors: \n" + np);

		int[] numArray = new int[NUM];
		for (int i = 0; i < NUM; i++) {
			numArray[i] = i + 1;
		}

		for (int numOfThreads = 1; numOfThreads <= np; numOfThreads++) {

			long startTime = System.currentTimeMillis();
			long result = parallel(numOfThreads, numArray);
			long endTime = System.currentTimeMillis();

			System.out.println("\nTime with " + numOfThreads + " threads: " + (endTime - startTime) + " ms.");

			printResult(NUM, result);
		}
	}

	public static void printResult(int num, long result) {
		System.out.print(num + "! = " + result);
	}

	public static long parallel(int numOfThreads, int[] numArray) {

		ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);

		int numberOfElementsInEachThread = numArray.length / numOfThreads;

		FactTask[] tasks = new FactTask[numOfThreads];

		int i;
		for (i = 0; i < numOfThreads - 1; i++) {
			tasks[i] = (new FactTask(i * numberOfElementsInEachThread, (i + 1) * numberOfElementsInEachThread,
					numArray));
			executor.execute(tasks[i]);
		}

		tasks[i] = (new FactTask(i * numberOfElementsInEachThread, numArray.length, numArray));
		executor.execute(tasks[i]);

		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		long result = 1;
		for (int k = 0; k < tasks.length; k++) {
			result *= tasks[k].getFact();
		}
		return result;
	}

	public static class FactTask implements Runnable {
		private int start;
		private int end;

		private long fact = 1;

		private int[] numArray;

		private static Lock lock = new ReentrantLock();

		FactTask(int start, int end, int[] numArray) {
			this.start = start;
			this.end = end;
			this.numArray = numArray;
		}

		public void run() {

			lock.lock();

			try {

				for (int i = start; i < end; i++) {
					fact = fact * numArray[i];
				}

			} finally {
				lock.unlock();
			}

		}

		public long getFact() {
			return fact;
		}
	}
}