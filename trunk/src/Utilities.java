import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilities {
	
	public static void killAllRserver(){
		Runtime rt = Runtime.getRuntime();
		if (OSValidator.isWindows()) {
			try {
				rt.exec("taskkill /F /IM  Rserve.exe");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				rt.exec("killall -9 Rserve");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void printInfo(){
		printInfo("");
	}
	public static void printInfo(String... errors){
		for(String error: errors){
			print("INFO: "+error);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}	
	
	public static void printError(String... errors){
		for(String error: errors){
			print("ERROR: "+error);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}
	
	private static void print(String str){

		System.out.println(DateFormat.getInstance().format(new Date())+"  "+str);
	}
	
	public static void printErrorAndExit(String... errors){
		printError(errors);
		exit();
	}	
	
	public static void exit() {
		// close();
		try {
			if (OSValidator.isWindows()) {
				printInfo("Please press any key to quit ......");
				System.in.read();
			} else {

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Utilities.killAllRserver();
		System.exit(-1);
	}
	
	//public static int port=3333;
	
	public static int getAvailablePort() {
		//return port++;
		
		try {
			ServerSocket socket = new ServerSocket(0);
			int port=socket.getLocalPort();
			socket.close();

			return port;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Utilities.printErrorAndExit("Finad avaiable port error");
			return -1;
		}
	
	
	}
	
	public static void killProcess(Process process){
		if(process.getClass().getName().equals("java.lang.UNIXProcess")) {
			  /* get the PID on unix/linux systems */
			  try {
			    Field f = process.getClass().getDeclaredField("pid");
			    f.setAccessible(true);
			    int pid = f.getInt(process);
			    Runtime.getRuntime().exec("kill "+pid);
			  } catch (Throwable e) {
			  }
			}
	}
	
	

}
