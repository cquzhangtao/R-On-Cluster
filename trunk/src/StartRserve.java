import java.io.*;
import java.util.*;
import org.rosuda.REngine.Rserve.RConnection;

/** helper class that consumes output of a process. In addition, it filter output of the REG command on Windows to look for InstallPath registry entry which specifies the location of R. */
class StreamHog extends Thread {
	InputStream is;
	boolean capture;
	String installPath;
	private String name;
	StreamHog(InputStream is, boolean capture,String name) {
		this.is = is;
		this.capture = capture;
		this.name=name;
		start();
	}
	public String getInstallPath() {
		return installPath;
	}
	public void run()
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ( (line = br.readLine()) != null) {
			//while ( true) {
				//if((line = br.readLine()) != null){
					if (capture) { // we are supposed to capture the output from REG command
						int i = line.indexOf("InstallPath");
						if (i >= 0) {
							String s = line.substring(i + 11).trim();
							int j = s.indexOf("REG_SZ");
							if (j >= 0)
								s = s.substring(j + 6).trim();
							installPath = s;
							Utilities.printInfo("R InstallPath = "+s);
						}
					} else 
					{
						Utilities.printInfo("R Message>> "+name+"> " + line);
					}
				//}
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}

/** simple class that start Rserve locally if it's not running already - see mainly <code>checkLocalRserve</code> method. It spits out quite some debugging outout of the console, so feel free to modify it for your application if desired.<p>
 <i>Important:</i> All applications should shutdown every Rserve that they started! Never leave Rserve running if you started it after your application quits since it may pose a security risk. Inform the user if you started an Rserve instance.
 */
public class StartRserve {
	/** shortcut to <code>launchRserve(cmd, "--no-save --slave", "--no-save --slave", false)</code> */
	public static Process launchRserve(String cmd,String rServePath,String name) { return launchRserve(cmd, "--no-save --slave","--no-save --slave",6311,false,rServePath,name); }
	public static Process launchRserve(String cmd,String rServePath,int port,String name) { return launchRserve(cmd, "--no-save --slave","--no-save --slave",port,false,rServePath,name); }
	
	/** attempt to start Rserve. Note: parameters are <b>not</b> quoted, so avoid using any quotes in arguments
	 @param cmd command necessary to start R
	 @param rargs arguments are are to be passed to R
	 @param rsrvargs arguments to be passed to Rserve
	 @return <code>true</code> if Rserve is running or was successfully started, <code>false</code> otherwise.
	 */
	public static Process launchRserve(String cmd, String rargs, String rsrvargs,int port, boolean debug,String rServePath,String name) {
		Process p;
		try {
			
			boolean isWindows = false;
			String osname = System.getProperty("os.name");
			
			if (osname != null && osname.length() >= 7 && osname.substring(0,7).equals("Windows")) {
				isWindows = true; /* Windows startup */
				p = Runtime.getRuntime().exec("\""+cmd+"\" -e \"Sys.setenv('R_LIBS_USER'='"+rServePath+"');library(Rserve);Rserve("+(debug?"TRUE":"FALSE")+",port="+port+",args='"+rsrvargs+"')\" "+rargs);
			} else{ /* unix startup */
				String[] com=new String[] {
					      "/bin/sh", "-c",
					      "echo '.libPaths(\""+rServePath+"\");library(Rserve);Rserve("+(debug?"TRUE":"FALSE")+",port="+port+",args=\""+rsrvargs+"\")'|"+cmd+" "+rargs
					      };
				//Utilities.printInfo(com);
				//String command=cmd+" CMD "+rServePath+"/Rserve/libs/Rserve --no-save --RS-port "+port;
				//command=cmd+" CMD "+rServePath+"/Rserve/libs/Rserve --no-save";
				p = Runtime.getRuntime().exec(com);
			}
//			p = Runtime.getRuntime().exec(new String[] {
//				      "/bin/sh", "-c",
//				      "echo 'Sys.setenv(\"R_LIBS_USER\"=\""+rServePath+"\");library(Rserve);Rserve("+(debug?"TRUE":"FALSE")+",args=\""+rsrvargs+"\")'|"+cmd+" "+rargs
//				      });
			Utilities.printInfo("waiting for Rserve to start ... ("+p+")");
			// we need to fetch the output - some platforms will die if you don't ...
			for(int i=0;i<20;i++){
			StreamHog errorHog = new StreamHog(p.getErrorStream(), false,name);
			StreamHog outputHog = new StreamHog(p.getInputStream(), false,name);
			
			}
			//StreamHog msgHog = new StreamHog(p.getOutputStream()., false);
			if (!isWindows) /* on Windows the process will never return, so we cannot wait */
				p.waitFor();
			//Utilities.printInfo("call terminated, let us try to connect ...");
		} catch (Exception x) {
			Utilities.printError("failed to start Rserve process, try again now ...");
			return null;
		}
		return p;
//		int attempts = 5; /* try up to 5 times before giving up. We can be conservative here, because at this point the process execution itself was successful and the start up is usually asynchronous */
//		while (attempts > 0) {
//			try {
//				RConnection c = new RConnection();
//				Utilities.printInfo("Rserve is running.");
//				c.close();
//				return true;
//			} catch (Exception e2) {
//				Utilities.printInfo("Try failed with: "+e2.getMessage());
//			}
//			/* a safety sleep just in case the start up is delayed or asynchronous */
//			try { Thread.sleep(500); } catch (InterruptedException ix) { };
//			attempts--;
//		}
		//return false;
	}

//	/** checks whether Rserve is running and if that's not the case it attempts to start it using the defaults for the platform where it is run on. This method is meant to be set-and-forget and cover most default setups. For special setups you may get more control over R with <<code>launchRserve</code> instead. */
//	public static boolean checkLocalRserve() {
//		if (isRserveRunning()) return true;
//		String osname = System.getProperty("os.name");
//		if (osname != null && osname.length() >= 7 && osname.substring(0,7).equals("Windows")) {
//			Utilities.printInfo("Windows: query registry to find where R is installed ...");
//			String installPath = null;
//			try {
//				Process rp = Runtime.getRuntime().exec("reg query HKLM\\Software\\R-core\\R");
//				StreamHog regHog = new StreamHog(rp.getInputStream(), true);
//				rp.waitFor();
//				regHog.join();
//				installPath = regHog.getInstallPath();
//			} catch (Exception rge) {
//				Utilities.printInfo("ERROR: unable to run REG to find the location of R: "+rge);
//				return false;
//			}
//			if (installPath == null) {
//				Utilities.printInfo("ERROR: canot find path to R. Make sure reg is available and R was installed with registry settings.");
//				return false;
//			}
//			return launchRserve(installPath+"\\bin\\R.exe");
//		}
//		return (launchRserve("R") || /* try some common unix locations of R */
//			((new File("/Library/Frameworks/R.framework/Resources/bin/R")).exists() && launchRserve("/Library/Frameworks/R.framework/Resources/bin/R")) ||
//			((new File("/usr/local/lib/R/bin/R")).exists() && launchRserve("/usr/local/lib/R/bin/R")) ||
//			((new File("/usr/lib/R/bin/R")).exists() && launchRserve("/usr/lib/R/bin/R")) ||
//			((new File("/usr/local/bin/R")).exists() && launchRserve("/usr/local/bin/R")) ||
//			((new File("/sw/bin/R")).exists() && launchRserve("/sw/bin/R")) ||
//			((new File("/usr/common/bin/R")).exists() && launchRserve("/usr/common/bin/R")) ||
//			((new File("/opt/bin/R")).exists() && launchRserve("/opt/bin/R"))
//			);
//	}
	
	/** check whether Rserve is currently running (on local machine and default port).
	 @return <code>true</code> if local Rserve instance is running, <code>false</code> otherwise
	 */
	public static boolean isRserveRunning() {
		try {
			RConnection c = new RConnection();
			Utilities.printInfo("Rserve is running.");
			c.close();
			return true;
		} catch (Exception e) {
			Utilities.printInfo("First connect try failed with: "+e.getMessage());
		}
		return false;
	}
	
	/** just a demo main method which starts Rserve and shuts it down again */
//	public static void main(String[] args) {
//		Utilities.printInfo("result="+checkLocalRserve());
//		try {
//			RConnection c=new RConnection();
//			c.shutdown();
//		} catch (Exception x) {};
//	}
}