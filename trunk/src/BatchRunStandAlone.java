import java.io.File;

import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.REXP;

public class BatchRunStandAlone extends AbstractBatchRun {
	private Rengine engine;
	
	public BatchRunStandAlone(){
		super();
		setConfigs(new Configuration(getAppPath()));
		//System.load(getConfigs().getRJavaPath()+File.separator+"") 
		//System.load("/usr/local/lib/R/site-library/rJava/libs/rJava.so");
	}
	


	@Override
	public REXPAdapter run(String loadScript) {
		
		org.rosuda.JRI.REXP re =engine.eval(loadScript);
		if(re==null){
			error("Script error.");
		}
		return new REXPAdapter(re);
		
	}

	@Override
	protected void close() {
		if(engine!=null){
			engine.end();
		}
		
	}



	@Override
	public void connect() {
		//System.out.println("Loading R ...");
		engine = new Rengine(new String[] { "--no-save" }, false, null);
        if (!engine.waitForR())
        {
            error ("Unable to load R");
        }
		
	}



	@Override
	public boolean runScriptFile(String name, String file) {
		run(file);
		return true;
		
	}



	@Override
	public void onOneScenarioDone(String scenarioName) {
		info(scenarioName+" is done!");
		
	}
}
