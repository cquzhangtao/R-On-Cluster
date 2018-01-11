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
        
        String installedLibFolder=getAppPath()+File.separator+"installed libs";
		
		if(!new File(installedLibFolder).exists()){
			new File(installedLibFolder).mkdirs();
		}
		installedLibFolder=installedLibFolder.replace("\\", "/");
		  engine.eval(".libPaths(\""+installedLibFolder+"\")");
		  
        installLocalLibs(installedLibFolder);
        
      
        
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
	
	public void installLocalLibs(String installedLibFolder){
		String libFolder=getAppPath()+File.separator+"lib";
	
		if(!new File(libFolder).exists()){
			error("Required librares are not found.");
		}
		engine.eval("Sys.setenv(\"R_LIBS_USER\"=\""+installedLibFolder+"\")");
		
		//REXP ret =engine.eval("installed.packages()[,c(1,3)]");
		//System.out.println(ret.asStringArray().toString());
		
		File[]files=new File(libFolder).listFiles();
		if(files==null&&files.length==0){
			error("Required librares are not found.");
		}
		
		for(File file:files){
			if(file.isDirectory()){
				continue;
			}
			
			String ext;
			if(OSValidator.isWindows()){
				ext=".zip";
			}else{
				ext=".tar.gz";
			}
			if(file.getName().toLowerCase().contains(ext)){
				String name=file.getAbsolutePath().replace("\\", "/");
				boolean installed=false;
				if(name.lastIndexOf("_")>-1&&name.lastIndexOf("/")+1<name.length()){
					String shortName=name.substring(name.lastIndexOf("/")+1,name.lastIndexOf("_"));
					System.out.println("Loading libaray "+shortName+" ...");
					REXP re =engine.eval("'"+shortName+"' %in% installed.packages()[,c(1,3)]");
					if(re!=null && re.asBool()!=null &&re.asBool().isTRUE()){
						installed=true;
						continue;
					}
				}
				
				REXP re =engine.eval("install.packages(\""+name+"\",\""+installedLibFolder+"\",repos = NULL, type=\"source\")");
				if(re==null){
					error("Cannot install library:"+name);
				}
			}
		}
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
