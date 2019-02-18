package subscriber;

/**
 * Created by Dhruv Naik on 4/8/2018.
 */

public enum ErrorMessage {

	NONE(""), SERVER_UP("Server alive"), SERVER_DOWN("Server down,, Try later.."), DISCONNECTED(
			"Session disconnected"), ALREADY_CONNECTED(
					"Subscriber already connected"), SERVER_INTERNAL_ERROR("Unknown error occurred in server");

	private final String msg;

	ErrorMessage(String msg) {
		this.msg = msg;
	}

	public String toString() {
		return msg;
	}

}