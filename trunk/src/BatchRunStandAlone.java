import java.io.File;
import java.io.IOException;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.Rserve.RConnection;

public class BatchRunStandAlone extends AbstractBatchRun {
	private Rengine engine;
	
	public BatchRunStandAlone(){
		
		System.out.println("Loading R ...");
		engine = new Rengine(new String[] { "--no-save" }, false, null);
        if (!engine.waitForR())
        {
            error ("Unable to load R");
        }
             
        REXP re =engine.eval("if (!'RSNNS' %in% installed.packages()) install.packages('RSNNS')");
        if(re==null){
			error("Cannot install package RSNNS.");
		}

        re =engine.eval("if(!'RSNNS' %in% (.packages())) library(RSNNS)");
        if(re==null){
			error("Cannot load library RSNNS.");
		}
        //re =engine.eval("install.packages('RSNNS')");
       // re =engine.eval("installed.packages()");
       // re =engine.eval(".libPaths()");
       //re =engine.eval("library('Rcpp')");
      // re =engine.eval("library('RSNNS')");
      // System.out.println(re);
        
	}
	

    


	@Override
	public void run(String loadScript) {
		
		REXP re =engine.eval(loadScript);
		if(re==null){
			error("Script error.");
		}
		
	}

	@Override
	protected void close() {
		if(engine!=null){
			engine.end();
		}
		
	}
}
