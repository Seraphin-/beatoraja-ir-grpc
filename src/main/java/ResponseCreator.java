package bms.player.beatoraja.ir;
import bms.player.beatoraja.ir.IRResponse;

public class ResponseCreator<T> {
    public IRResponse<T> create(final boolean success, final String msg, final T data) {
        return new IRResponse<T>() {
        public boolean isSucceeded() { return success; }
        public String getMessage() { return msg; }
        public T getData() { return data; }
        };
    }
}
