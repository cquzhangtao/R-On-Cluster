import java.io.IOException;

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
			System.out.println("INFO: "+error);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}	
	
	public static void printError(String... errors){
		for(String error: errors){
			System.out.println("ERROR: "+error);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
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
	
	

}
