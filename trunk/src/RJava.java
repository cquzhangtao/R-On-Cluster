import java.io.File;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

public class RJava {
    public static void main(String a[]) {
    	//
    	//System.loadLibrary("Rblas");
    	
    	//System.setProperty("java.library.path", "./");
    	//System.out.println(System.getProperty("java.library.path"));
    	//System.loadLibrary("r");
    	// Create an R vector in the form of a string.
        String javaVector = "c(1,2,3,4,5)";
//D:\tao\OSRAMRClient\script.txt
        // Start Rengine.
        Rengine engine = new Rengine(new String[] { "--no-save" }, false, null);
        if (!engine.waitForR())
        {
            System.out.println ("Unable to load R");
            return;
        }
        else
            System.out.println ("Connected to R");

        // The vector that was created in JAVA context is stored in 'rVector' which is a variable in R context.
        engine.eval("rVector=" + javaVector);
        
        //Calculate MEAN of vector using R syntax.
        engine.eval("meanVal=mean(rVector)");
        
        //Retrieve MEAN value
        double mean = engine.eval("meanVal").asDouble();
       
        //Print output values
        System.out.println("Mean of given vector is=" + mean);
       REXP re =engine.eval("if (!'RSNNS' %in% installed.packages()) install.packages('RSNNS')");
       re =engine.eval("install.packages('RSNNS')");
       re =engine.eval("installed.packages()");
       re =engine.eval(".libPaths()");
     // re =engine.eval("library('Rcpp')");
      re =engine.eval("library('RSNNS')");
       re =engine.eval("if(!'RSNNS' %in% (.packages())) library(RSNNS)");
       re =engine.eval(".packages()");
        //REXP re = engine.eval("install.packages('RSNNS')");
        String loadScript="source('D:/tao/OSRAMRClient/script.txt')";
        //loadScript=loadScript.replace("\\", "/");
        re = engine.eval(loadScript);
        
     
        System.out.println(engine.eval("timeHorizon").asDouble());
        
       engine.end();

    }
}
