
public class SplitRun extends ParallelBatchRunOnServer{
	
	public SplitRun(){
		super();
		 
		ScenarioSplitter.createScenarios(this.getConfigs().getWorkingPath());
		
	}

}
