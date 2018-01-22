import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Configuration extends BasicLogger{

	private Map<String,String> configs=new HashMap<String,String>();
	
	public Configuration(String path){
		readConfigs(path);
	}
	
	private void readConfigs(String path){
		
		 info("Reading configuration...");
		List<String> configsList = null;
		try{
			configsList=Files.readAllLines(Paths.get(path+File.separator+"config.txt"));
		}catch(IOException e){
			try{
				Charset charset = Charset.forName("Cp1252");
				configsList=Files.readAllLines(Paths.get(path+File.separator+"config.txt"),charset);
			}catch(IOException e1){
				e1.printStackTrace();
				error("Config file does not exist or error!");
			}
		}
		 
		 int lineNum=0;
		for(String config:configsList){
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
			String paraName=str[0].trim().toUpperCase();
			String paraValue=str[1].trim();
			if(paraName.isEmpty()||paraValue.isEmpty()){
				syntaxError(lineNum,config);
			}
			configs.put(paraName, paraValue);
		}

		
}
	protected void syntaxError(int lineNum, String content) {
		error("Syntax error in the config file:", "Line " + lineNum + " :"
				+ content);
	}
	
	public String getWorkingPath(){
	
		return getPath("WORKING_PATH");
	}
	
	private String getPath(String pathname){
		String path=getParameter(pathname);
		if(!new File(path).exists()){
			error("The specified "+pathname+" does not exist.",path);
		}
		return path;
	}
	
	public String getRPath(){
		return getPath("R_PATH");
	}
	public String getRServePath(){
		return getPath("RSERVE_PATH");
	}
	public String getRJavaPath(){
		return getPath("RJAVA_PATH");
	}
	
	private String getParameter(String name){
		String value=configs.get(name);
		if(value==null){
			error("Parameter:"+name+" is not defined in the config file.");
		}
		return value;
	}

}
