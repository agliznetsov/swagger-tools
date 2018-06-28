package {{package}};

import org.apache.http.Header;

import java.util.List;

public class HttpStatusException extends RuntimeException {
    private final int statusCode;
    private final String statusText;
    private final byte[] responseBody;
    private final List<Header> responseHeaders;

    public HttpStatusException(int statusCode, String statusText, byte[] responseBody, List<Header> responseHeaders) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.responseBody = responseBody;
        this.responseHeaders = responseHeaders;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public List<Header> getResponseHeaders() {
        return responseHeaders;
    }
}
