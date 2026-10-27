package ensdfparser.base;

public abstract class BaseControl {
	//common variable (value) for all subclasses
	//Note that when running multiple apps together, 
	//change in errorLimit in the control subclass 
	//associated with one app will also apply to
	//all other control subclasses. So use it with 
	//caution
	public static int errorLimit =35;
	public static String workdir="";
}
