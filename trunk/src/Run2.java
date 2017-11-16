import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class Run2 {

	public static void main(String[] args) {
		System.out.println();
		System.out.println("OSRAM R Client @Unibw Munich 11-09-2017");
		
		RConnection connection = null;
		 
        try {
        	
        	String path=Run2.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        	
        	if(path.contains("jar")){
        		if(path.contains(":/")){
        			path=path.substring(1, path.lastIndexOf("/")).replace("/", File.separator);
        		}else{
        			path=path.substring(0, path.lastIndexOf("/")).replace("/", File.separator);
        		}
        	} 
        	else{
        		path=path.substring(1, path.lastIndexOf("/bin/")).replace("/", File.separator);
        	}
        	System.out.println(path);
        	generateScript(path);

        	System.out.println("Connectting to Rserve server...");
            connection = new RConnection();
            
            System.out.println("Executing the script on Rserve server...");
            String loadScript="source('"+path+File.separator+"script.txt')";
            loadScript=loadScript.replace("\\", "/");
            connection.eval(loadScript);

            System.out.println("Executed!");
           
            
        } catch (RserveException e) {
           // e.printStackTrace();
        	error("Two reasons lead to this error:",
        			" 1)Rserve server is not started. Please install the server if necessary and start it in your R console and run this again."
        	 		+ "-- install.packages(\"Rserve\")-> library(Rserve)-> Rserve();", 
        	 		" 2)Template file has syntax errors. Please check the file scripttemp.txt.");

        } catch (URISyntaxException e) {
			
			//e.printStackTrace();
			error("Exception on File path.");
		}finally{
            connection.close();
        }
        exit();

	}
	
	private static void generateScript(String path){
		try {
			 System.out.println("Reading configuration...");
			List<String> configs=Files.readAllLines(Paths.get(path+File.separator+"config.txt"));
			 System.out.println("Reading templates...");
			String template= new String(Files.readAllBytes(Paths.get(path+File.separator+"scripttemp.txt")));
			 System.out.println("Generating script...");
			 int lineNum=0;
			for(String config:configs){
				lineNum++;
				config=config.trim();
				if(config.isEmpty()||config.startsWith("//")){
					continue;
				}
				if(!config.contains("::=")){
					syntaxError(lineNum,config);
				}
				if(config.contains("//")&&config.indexOf("::=")>config.indexOf("//")){
					syntaxError(lineNum,config);
				}
				config=config.substring(0, config.indexOf("//")>0?config.indexOf("//"):config.length());
								
				String str[]=config.split("::=");
				String paraName=str[0].trim();
				String paraValue=str[1].trim();
				if(paraName.isEmpty()||paraValue.isEmpty()){
					syntaxError(lineNum,config);
				}
				template=template.replace(paraName, paraValue);
			}
			new File(path+File.separator+"script.txt").delete();
			try(  PrintWriter out = new PrintWriter( path+File.separator+"script.txt" )  ){
			    out.println( template );
			}
			if(template.contains("_Para_")){
				error("One or more parameters are not defined in the config file.");
			}
	
			System.out.println("Script is generated.");
			
		} catch (IOException e) {
			e.printStackTrace();
			error("Config file or template file is broken.");
		}
	}
	
	private static void syntaxError(int lineNum, String content){
		error("Syntax error in the config file:", "Line "+lineNum+" :"+content);
	}
	
	private static void error(String... errors){
		for(String error: errors){
			System.err.println("ERROR: "+error);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
		System.out.println("The client is terminated.");
		exit();
	}
	
	private static void exit(){
		try {
			System.out.println("Please press any key to quit ......");
			System.in.read();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.exit(-1);
	}

}
