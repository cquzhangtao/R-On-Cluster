import org.rosuda.REngine.REXPMismatchException;






public class REXPAdapter {
	private org.rosuda.REngine.REXP exp1;
	private org.rosuda.JRI.REXP exp2;
	
	public REXPAdapter(org.rosuda.REngine.REXP exp){
		exp1=exp;
	}
	public REXPAdapter(org.rosuda.JRI.REXP exp){
		exp2=exp;
	}
	
	public boolean isNull(){
		return exp1==null&&exp2==null;
	}
	
	public boolean isBoolean(){
		if(exp1!=null){
			try {
			return exp1.asString()!=null&&(exp1.asString().equalsIgnoreCase("TRUE")||exp1.asString().equalsIgnoreCase("FALSE"));
			} catch (REXPMismatchException e) {
				return false;
			}
		}else{
			return exp2.asBool()!=null;
		}
	}
	
	public boolean isTrue(){
		if(exp1!=null){
			try {
			if(exp1.asString().equalsIgnoreCase("TRUE")){
				return true;
			}
			return false;
			} catch (REXPMismatchException e) {
				return false;
			}
		}else{
			return exp2.asBool().isTRUE();
		}
	}
	
	
	public boolean isString(){
		if(exp1!=null){
			return exp1.isString();
		}else{
			return exp2.asString()!=null;
		}
	}
	
	public String asString(){
		if(exp1!=null){
			try {
				return exp1.asString();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				return null;
			}
		}else{
			return exp2.asString();
		}
	}

}
