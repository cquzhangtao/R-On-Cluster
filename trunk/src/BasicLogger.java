


public class BasicLogger {
	
	public void info(String... errors) {
		if (errors.length == 0) {
			Utilities.printInfo();
		} else {
			Utilities.printInfo(errors);
		}
	}

	public void errorNoExit(String... errors) {
		Utilities.printError(errors);
	}

	public void error(String... errors) {
		Utilities.printErrorAndExit(errors);

	}

	

}
