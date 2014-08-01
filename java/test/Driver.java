package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import test.stati.Stati;
import test.work.SnsWorker;
import test.work.Worker;


public class Driver {
	/**
	 * client-count time(sec) url request-interval client-start-id
	 * jettyserver: 300 10 http://localhost 2 1
	 * 
	 * logicserver: 1 100 http://203.75.148.134:9099/SnsGame/SnsHttpAccess 2 1
	 * 
	 * llp: 1 100 http://192.168.2.147:9101/SnsGame/SnsHttpAccess 2000 100000
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		testGame(args);
	}
	
	static void testNginx(String[] args) throws Exception {
		int workerCount = Integer.parseInt(args[0]);
		int testTimeMin = Integer.parseInt(args[1]);
		
		Stati stati = new Stati();
		CountDownLatch cdLatch = new CountDownLatch(workerCount);
		Worker worker = new Worker(stati, cdLatch, args[2], args[3]);
		System.out.println("start to test with " + workerCount + " workers, and test " + 
				testTimeMin + " minutes");
		List<Thread> workers = new ArrayList<>(workerCount);
		for (int i = 0; i < workerCount; ++i) {
			Thread th = new Thread(worker, "worker" + i);
			th.start();
			workers.add(th);
		}
		
		Thread.sleep(TimeUnit.MINUTES.toMillis(testTimeMin));
		worker.close();
		cdLatch.await();
		System.out.println("test end");
		System.out.println(stati);
		/*
		 * test end
total request count:542499, total failed request count:0, total request cost time:600061(milis),averge request cost time:1.1061052647101655(milis), max response cost time:46(milis), min response cost time:0(milis)
		 */
	}
	
	private static void testGame(String[] args) throws Exception {
		int workerCount = Integer.parseInt(args[0]);
		int testTimeMin = Integer.parseInt(args[1]);
		
		Stati stati = new Stati();
		CountDownLatch cdLatch = new CountDownLatch(workerCount);
		System.out.println("start to test with " + workerCount + " workers, and test " + 
				testTimeMin + " minutes");
		SnsWorker.initialize(args[4]);
		List<Worker> workers = new ArrayList<>(workerCount);
		for (int i = 0; i < workerCount; ++i) {
			SnsWorker w = new SnsWorker(stati, cdLatch, args[2], args[3]);
			Thread th = new Thread(w, "worker" + i);
			th.start();
			workers.add(w);
		}
		
		Thread.sleep(TimeUnit.MINUTES.toMillis(testTimeMin));
		for (Worker w : workers) {
			w.close();
		}
		cdLatch.await();
		System.out.println("test end");
		System.out.println(stati);
	}
}
