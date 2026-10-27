package ensdfparser.calc;

public class InvalidFunctionResultException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFunctionResultException(String errorMessage, Throwable err) {
	    super(errorMessage, err);
	}
}
