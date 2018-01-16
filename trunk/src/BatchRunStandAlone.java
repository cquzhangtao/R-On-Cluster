import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.REXP;

public class BatchRunStandAlone extends AbstractBatchRun {
	private Rengine engine;
	
	public BatchRunStandAlone(){
		super();
		setWayToIntegerateJava(1);
		
        

        

        
      
        
//        REXP re =engine.eval("library('boot')");
//        if(re==null){
//			error("Cannot install package boot.");
//		}
             
//        REXP re =engine.eval("if (!'RSNNS' %in% installed.packages()) install.packages('RSNNS')");
//        if(re==null){
//			error("Cannot install package RSNNS.");
//		}
//
//        REXP re =engine.eval("if(!'RSNNS' %in% (.packages())) library('RSNNS')");
//        if(re==null){
//			error("Cannot load library RSNNS.");
//		}
        
        
        
        //re =engine.eval("install.packages('RSNNS')");
       // re =engine.eval("installed.packages()");
       // re =engine.eval(".libPaths()");
       //re =engine.eval("library('Rcpp')");
      // re =engine.eval("library('RSNNS')");
      // System.out.println(re);
      //  install.packages(<pathtopackage>, repos = NULL, type="source")
        
	}
	

	

    


	@Override
	public REXP run(String loadScript) {
		
		org.rosuda.JRI.REXP re =engine.eval(loadScript);
		if(re==null){
			error("Script error.");
		}
		return null;
		
	}

	@Override
	protected void close() {
		if(engine!=null){
			engine.end();
		}
		
	}







	@Override
	public void start() {
		//System.out.println("Loading R ...");
		engine = new Rengine(new String[] { "--no-save" }, false, null);
        if (!engine.waitForR())
        {
            error ("Unable to load R");
        }
		
	}
}
