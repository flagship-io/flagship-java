import com.abtasty.flagship.api.HttpHelper;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.Visitor;
import org.json.JSONObject;
import org.junit.After;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class, HttpHelper.class })
public class FlagshipIntegrationTests {

    private final HashMap<String, OnRequestValidation>  requestToVerify             = new HashMap<>();
    private final HashMap<String, HttpURLConnection>    responseToMock              = new HashMap<>();
    private Boolean                                     requestsVerified            = true;
    private Boolean                                     missingRequestVerification  = false;

    interface OnRequestValidation {
        void onRequestValidation(Response response);
    }

    public void verifyRequest(String url, OnRequestValidation validation) {
        if (url != null && validation != null)
            requestToVerify.put(url, validation);
    }

    public void mockResponse(String url, int code, String content) {
        if (url != null && code > 0 && content != null) {
            HttpURLConnection connection = PowerMockito.mock(HttpURLConnection.class);
            try {
                PowerMockito.when(connection.getResponseCode()).thenReturn(code);
                PowerMockito.when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes()));
                PowerMockito.when(connection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
                PowerMockito.when(connection.getErrorStream()).thenCallRealMethod();
                PowerMockito.when(connection.getResponseMessage()).thenCallRealMethod();
            } catch (Exception e) {
                e.printStackTrace();
            }
            responseToMock.put(url, connection);
        }
    }

    @Before
    public void before() {

        requestsVerified = true;
        missingRequestVerification = false;
        responseToMock.clear();
        requestToVerify.clear();

        try {
            spy(HttpHelper.class);
            doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    URL url = invocation.getArgument(0);
                    if (responseToMock.containsKey(url.toString()))
                        return responseToMock.get(url.toString());
                    else
                        return invocation.callRealMethod();
                }
            }).when(HttpHelper.class, "createConnection", any());

            doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Response response = (Response) invocation.callRealMethod();
                    if (requestToVerify.containsKey(response.requestUrl)) {
                        try {
                            requestToVerify.get(response.getRequestUrl()).onRequestValidation(response);
                        } catch (Error | Exception e) {
                            requestsVerified = false;
                            System.err.println("Error verifying : " + response.requestUrl);
                        }
                    } else {
                        missingRequestVerification = true;
                        System.err.println("Error url not verified : " + response.requestUrl);
                    }
                    return response;
                }
            }).when(HttpHelper.class, "parseResponse", any(), any(), any(), any(), any());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @After
    public void after() {
        assertTrue(requestsVerified);
        assertFalse(missingRequestVerification);
    }

    @Test
    public void synchronize() {

        try {
            mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true&sendContextEvent=false", 200, FlagshipIntegrationConstants.synchronizeResponse);

            verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true&sendContextEvent=false", (request) -> {
                assertTrue(request.getType().toString().equalsIgnoreCase("POST"));
                assertTrue(request.getRequestHeaders().containsKey("x-api-key"));
                assertEquals(request.getRequestHeaders().get("x-api-key"), "my_api_key");
                JSONObject content = new JSONObject(request.requestContent);
                assertTrue(content.has("context"));
                assertTrue(content.getJSONObject("context").getBoolean("vip"));
                assertEquals(content.getJSONObject("context").getInt("age"), 32);
                assertTrue(content.has("visitorId"));
                assertEquals(content.get("visitorId"), "visitor_1");
            });

            CountDownLatch synchronizeLatch = new CountDownLatch(1);
            Flagship.start("my_env_id", "my_api_key");
            Visitor visitor = Flagship.newVisitor("visitor_1", new HashMap<String, Object>() {{
                put("vip", true);
                put("age", 32);
            }});
            assert visitor != null;
            visitor.synchronizeModifications(() -> {
                assertEquals(visitor.getModification("isref", "default"), "not a all");
                assertEquals(visitor.getModification("release", 0), 100);
                synchronizeLatch.countDown();
            });
            if (!synchronizeLatch.await(1, TimeUnit.SECONDS))
                fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
