import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

public class ParallelBatchRunOnServer extends BatchRunOnServer {

	private List<ServerThread> threads = new ArrayList<ServerThread>();
	private Process process = null;

	public ParallelBatchRunOnServer() {

		super(0);

		int port = Utilities.getAvailablePort();
		this.setPort(port);

		setConfigs(new Configuration(getAppPath()));
		process = ServerThread.startServer(getConfigs().getRPath(),
				getConfigs().getRServePath(), port, "Main");
	}

	@Override
	public void onScenariosStart() {
		// List<ServerThread> failedThreads=new ArrayList<ServerThread>();
		//for (ServerThread thread : threads) {
		for(int i=0;i<threads.size();i++){
			ServerThread thread=threads.get(i);
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String str = "";

		for (ServerThread thread : threads) {
			if (!thread.isSucceed()) {
				str += thread.getServer().getScenarioName() + " ";
			}
		}

		info();
		info();

		if (!str.isEmpty()) {
			info("Scenarios " + str + " failed");
		} else {
			info("All Scenarios are executed successfully");
		}

		info("Batch execution is done!");
		close();
		Utilities.exit();

		// killProcesses();
		// onScenariosDone();

	}

	public void onOneScenariosStart() {

	}

	private void killProcesses() {
		// for(ServerThread thread:threads){
		// if(thread.getProcess()!=null){
		// thread.getProcess().destroyForcibly();
		// }
		// }
		close();
		if (process != null) {
			process.destroyForcibly();
		}

		// for(ServerThread thread:threads){
		// //thread.close();
		// try {
		// int
		// rServerPid=thread.getServer().getConnection().eval("Sys.getpid()").asInteger();
		// this.getConnection().eval("tools::pskill("+ rServerPid + ")");
		// this.getConnection().eval("tools::pskill("+ rServerPid +
		// ", tools::SIGKILL)");
		// } catch (RserveException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (REXPMismatchException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// BatchRunStandAlone run=new BatchRunStandAlone();
		// run.start();
		// try {
		// int rServerPid=getConnection().eval("Sys.getpid()").asInteger();
		// run.run("tools::pskill("+ rServerPid + ")");
		// run.run("tools::pskill("+ rServerPid + ", tools::SIGKILL)");
		// } catch (RserveException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (REXPMismatchException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// run.close();

	}

	//
	// public void runScenario(List<String>scenario,List<String>header,String
	// path){
	// info();
	// generateScriptForScenario(header,scenario,path);
	// String scenarioName=getScenarioName(header,scenario);
	//
	// String loadScript="source('"+path+"')";
	// loadScript=loadScript.replace("\\", "/");
	// runScriptFile(loadScript,scenarioName);
	// //info("Scenario "+scenarioName+" is executed!");
	// }
	
	ReentrantLock tlock = new ReentrantLock();

	@Override
	public boolean runScriptFile(String scenarioName, String script) {

		ServerThread thread = new ServerThread(getConfigs().getRPath(),
				getConfigs().getRServePath(), scenarioName, this);
		tlock.lock();
		threads.add(thread);
		tlock.unlock();
		// thread.setScenarioName(scenarioName);
		thread.setScript(script);
		thread.getServer().setScenarioName(scenarioName);
		info("Executing the script of Scenario " + scenarioName + " ...");
		thread.start();
		return true;

	}

	@Override
	public void onOneScenarioDone(String scenarioName) {
		// keep empty

	}

	private int maxProcessNum = 25;
	private int counter = 1;
	private List<List<String>> scenarios;

	public void runScenariosNewNew() {
		info();
		info("Starting to run scenarios");
		counter += maxProcessNum;
		scenarios = ScenarioUtilities.readScenarios(getConfigs()
				.getWorkingPath());

		int startedNum = 0;
		int index = 1;
		while (index < scenarios.size()
				&& startedNum < Math.min(scenarios.size(), maxProcessNum)) {
			List<String> scenario = scenarios.get(index);
			index++;
			if (isScenarioDisable(scenarios.get(0), scenario)) {

				continue;
			}

			runScenario(scenario, scenarios.get(0), getConfigs()
					.getWorkingPath());
			startedNum++;
		}

		// scenarios.parallelStream().forEach(ABC::method);
		
		onScenariosStart();
	}

	ReentrantLock lock = new ReentrantLock();

	public void onOneScenarioDoneForLimitedBatchSize() {
		
		lock.lock();
	
			//System.out.println("aaa"+counter+"bbb"+scenarios.size());
			if (counter >= scenarios.size()) {
				// info();
				// info("Batch execution is done!");
				// close();
				// Utilities.exit();
				lock.unlock();
				return;
			}
			List<String> scenario = scenarios.get(counter);
			while (counter < scenarios.size()
					&& isScenarioDisable(scenarios.get(0), scenario)) {

				counter++;
				scenario = scenarios.get(counter);
			}
			if (counter >= scenarios.size()) {
				// info();
				// info("Batch execution is done!");
				// close();
				// Utilities.exit();
				lock.unlock();
				return;
			}
			counter++;
			//System.out.println("rrrrraaa"+counter+"bbb"+scenarios.size());
	
			lock.unlock();
		

			runScenario(scenario, scenarios.get(0), getConfigs()
					.getWorkingPath());
			try {
				threads.get(threads.size() - 1).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		

	}
//	
//	@Override
//	public void connect() {
//		super.connect();
//		run(".libPaths('"+getConfigs().getRServePath()+"')");
//		REXPAdapter a = run(".libPaths()");
//		System.out.println(a.asString());
//	}

}
