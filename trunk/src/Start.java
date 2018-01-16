import java.io.IOException;

public class Start {

	public static void main(String[] args) {
		
		 
		Utilities.killAllRserver();
		if (args.length > 0) {
			return;
		}
		
		//System.out.println("Default Charset=" + Charset.defaultCharset());
		ParallelBatchRunOnServer tool=new ParallelBatchRunOnServer();
		//AbstractBatchRun tool=new BatchRunOnServer();
		//AbstractBatchRun tool=new BatchRunStandAlone();
		tool.printTitle();
		tool.start();
		tool.init();
		tool.installLocalLibs(tool.getRPath());
		tool.runScenarios();
		tool.exit();
	}

}
