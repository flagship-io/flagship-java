import com.abtasty.flagship.api.HttpHelper;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.Visitor;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class, HttpHelper.class })
public class FlagshipIntegrationTests {

    class Request {
        String                  type = null;
        String                  url = null;
        HashMap<String, String> headers = new HashMap<String, String>();
        JSONObject              body = new JSONObject();

        public Request(Response r) {
            this.type = r.getType().toString();
            this.url = r.getRequestUrl();
            this.headers = r.getRequestHeaders();
            try {
                this.body = new JSONObject(r.getRequestContent());
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public Request(String type, String url, HashMap<String, String> headers, JSONObject body) {
            this.type = type;
            this.url = url;
            this.headers = headers;
            this.body = body;
        }

        @Override
        public String toString() {
            return "Request{" +
                    "type='" + type + '\'' +
                    ", url='" + url + '\'' +
                    ", headers=" + headers +
                    ", body=" + body +
                    '}';
        }
    }

    interface OnRequestValidation {
        void onRequestValidation(Request request);
    }

    HashMap<String, OnRequestValidation> requestToVerify = new HashMap<>();

    public void verifyRequest(String url, OnRequestValidation validation) {
        if (url != null && validation != null)
            requestToVerify.put(url, validation);
    }

    @Before
    public void before() {
        System.out.println("FFFFFFFFFFFFFFFFFFFFF");
    }

    Boolean requestsVerified = true;
    Boolean missingRequestVerification = false;
    @Test
    public void first() {

        try {

            HttpURLConnection connection = PowerMockito.mock(HttpURLConnection.class);
//            connection.setDoOutput(true);
//            PowerMockito.mockStatic(HttpHelper.class);
//            when(HttpHelper.sendHttpRequest(any(), any(), any(), any())).thenCallRealMethod();
//            when(HttpHelper.class, "parseResponse", connection).thenCallRealMethod();
//            when(HttpHelper.createConnection(any())).thenReturn(connection); //Pass right URL
            PowerMockito.stub(PowerMockito.method(HttpHelper.class, "createConnection")).toReturn(connection);

            spy(HttpHelper.class);
            doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Response response  = (Response) invocation.callRealMethod();
                    if (requestToVerify.containsKey(response.requestUrl)) {
                        Request request = new Request(response);
                        try {
                            requestToVerify.get(response.getRequestUrl()).onRequestValidation(request);
                            requestsVerified = true;
                        } catch (Error e) {
                            requestsVerified = false;
                            System.err.println("Error verifying : " + request.toString());
                        }
                    }
                    else {
                        missingRequestVerification = true;
                        System.err.println("Error url not verified : " + response.requestUrl);
                    }
                    return response;
                }
            }).when(HttpHelper.class, "parseResponse", any(), any(), any(), any(), any());


            verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true&sendContextEvent=false", (request) -> {
                assertTrue(request.type.equalsIgnoreCase("POST"));
                assertTrue(request.headers.containsKey("x-api-key"));
                assertEquals(request.headers.get("x-api-key"), "my_api_key");
            });


            PowerMockito.when(connection.getResponseCode()).thenReturn(200);
            PowerMockito.when(connection.getContent()).thenReturn(200);
            String json = "{\"visitorId\":\"toto3\",\"campaigns\":[{\"id\":\"bmsorfe4jaeg0gi1bhog\",\"variationGroupId\":\"bmsorfe4jaeg0gi1bhpg\",\"variation\":{\"id\":\"bmsorfe4jaeg0gi1bhq0\",\"modifications\":{\"type\":\"FLAG\",\"value\":{\"featureEnabled\":true}},\"reference\":false}},{\"id\":\"bv6vpqjh6h101fk8lbrg\",\"variationGroupId\":\"bv6vpqjh6h101fk8lbsg\",\"variation\":{\"id\":\"bv6vpqjh6h101fk8lbtg\",\"modifications\":{\"type\":\"JSON\",\"value\":{\"isref\":\"nope\"}},\"reference\":false}},{\"id\":\"c04bed3m649g0h9nkui0\",\"variationGroupId\":\"c04bed3m649g0h9nkuj0\",\"variation\":{\"id\":\"c04bed3m649g0h9nkuk0\",\"modifications\":{\"type\":\"FLAG\",\"value\":{\"release\":100}},\"reference\":false}}]}";
//            String json = "{\"panic\":\"true\"}";
            PowerMockito.when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(json.getBytes()));
            PowerMockito.when(connection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
            PowerMockito.when(connection.getResponseCode()).thenReturn(100);
            PowerMockito.when(connection.getErrorStream()).thenCallRealMethod();
            PowerMockito.when(connection.getResponseMessage()).thenCallRealMethod();



            Flagship.start("my_env_id", "my_api_key");
            Visitor visitor = Flagship.newVisitor("toto");
            visitor.synchronizeModifications(() -> {

            });
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(requestsVerified);
        assertFalse(missingRequestVerification);

    }

    @Test
    public void test3() {
        System.out.println("integration");
    }

    @Test
    public void test4() {
        System.out.println("integration 4");
    }
}
