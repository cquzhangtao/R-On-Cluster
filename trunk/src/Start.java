import java.util.Scanner;



public class Start {

	public static void main(String[] args) {

		printTitle();
		if (args.length > 0) {
			if(args[0].trim().equalsIgnoreCase("kill")){
				Utilities.killAllRserver();
				Utilities.printInfo("All Rserve.exe processes are killed");
			}else if(args[0].trim().startsWith("-S-")||args[0].trim().startsWith("-s-")) {
				singleScriptFileRun(args[0].trim().substring(3));
			}
			else if(args[0].trim().equalsIgnoreCase("-C")) {
				commandConsole();
			}
			return;
		}
		
		//parallelRunOnRserve();
		//serialRunOnRserve();
		//serialRunOnRJava();
		splitParallelRunOnRserve();
	}

	private static void parallelRunOnRserve() {
		ParallelBatchRunOnServer tool = new ParallelBatchRunOnServer();
		tool.start();

		tool.installLocalLibs();
		tool.runScenariosNewNew();
	}
	
	private static void singleScriptFileRun(String file){
		SingleScriptFileRun tool=new SingleScriptFileRun();
		tool.start();
		tool.installLocalLibs();
		String loadScript = "source('" + file + "')";
		//loadScript="invisible(capture.output("+loadScript+"))";
		loadScript = loadScript.replace("\\", "/");
		tool.runScriptFile("Single", loadScript);
		tool.close();
	}
	
	private static void commandConsole(){
		
		Scanner scanner = new Scanner(System.in);
		SingleScriptFileRun tool=new SingleScriptFileRun();
		tool.start();
		tool.installLocalLibs();
		while(true){
			System.out.print(">>");
			String command= scanner.nextLine().trim();
			if(command.equalsIgnoreCase("q")){
				break;
			}
			
			REXPAdapter result = tool.run(command);
			if(result.isNull()){
				
			}else{
				System.out.println(result.asString());
			}
			System.out.println();
		}
		scanner.close();
		tool.close();
	}
	
	private static void splitParallelRunOnRserve() {
		SplitRun tool = new SplitRun();
		tool.start();

		tool.installLocalLibs();
		tool.runScenariosNewNew();
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
	


//library(rnn)
//
//## Not run:
//# create training numbers
//X1 = sample(0:127, 700, replace=TRUE)
//X2 = sample(0:127, 700, replace=TRUE)
//# create training response numbers
//Y <- X1 + X2
//# convert to binary
//X1 <- int2bin(X1, length=8)
//X2 <- int2bin(X2, length=8)
//Y  <- int2bin(Y,  length=8)
//# create 3d array: dim 1: samples; dim 2: time; dim 3: variables
//X <- array( c(X1,X2), dim=c(dim(X1),2) )
//# train the model
//model <- trainr(Y=Y[,dim(Y)[2]:1],
//X=X[,dim(X)[2]:1,],
//learningrate   =  0.1,
//hidden_dim     = 1,numepochs = 30
//)
//
//sink("[[ScenarioPath]]/ok.txt")
//cat("bbb")
//sink()


}
