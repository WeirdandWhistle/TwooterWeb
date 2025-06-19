package lib;

public class JSON {
	/**
	 * <ul>
	 * all ' ' ' / single quote is converted to a ' " ' / double quote<br>
	 * adds brakets the start and end<br>
	 * converts ' \[ ' to ' ' '
	 * </ul>
	 * 
	 * @param a
	 *            the String to convert to valid json
	 * @return the prossed JSON of a
	 */
	public static String make(String a) {

		a = a.replace("\"", "\\\"");

		a = a.replace("'", "\"");

		return "{" + a + "}";

	}

}
