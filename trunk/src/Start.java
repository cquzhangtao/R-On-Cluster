

public class Start {

	public static void main(String[] args) {

		printTitle();
		if (args.length > 0) {
			Utilities.killAllRserver();
			Utilities.printInfo("All Rserve.exe processes are killed");
			return;
		}
		
		//parallelRunOnRserve();
		//serialRunOnRserve();
		//serialRunOnRJava();
		splitParallelRunOnRserve();
	}

	private static void parallelRunOnRserve() {
		AbstractBatchRun tool = new ParallelBatchRunOnServer();
		tool.start();

		tool.installLocalLibs();
		tool.runScenarios();
	}
	
	private static void splitParallelRunOnRserve() {
		AbstractBatchRun tool = new SplitRun();
		tool.start();

		tool.installLocalLibs();
		tool.runScenarios();
	}

	private static void serialRunOnRserve() {
		AbstractBatchRun tool = new BatchRunOnServer();
		tool.start();
		tool.installLocalLibs();
		tool.runScenarios();
		tool.onScenariosDone();
	}
	
	private static void serialRunOnRJava() {
		
		AbstractBatchRun tool=new BatchRunStandAlone();
		tool.start();
		tool.installLocalLibs();
		tool.runScenarios();
		tool.onScenariosDone();
	}
	
	public static void printTitle() {
		System.out.println();
		System.out.println("R Experiment Tool @Unibw Munich 11-09-2017");
		System.out.println();
	}

}
