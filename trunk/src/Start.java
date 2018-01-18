

public class Start {

	public static void main(String[] args) {

		Utilities.killAllRserver();
		if (args.length > 0) {
			return;
		}
		printTitle();
		parallelRunOnRserve();
		//serialRunOnRserve();
		//serialRunOnRJava();
	}

	private static void parallelRunOnRserve() {
		AbstractBatchRun tool = new ParallelBatchRunOnServer();
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
