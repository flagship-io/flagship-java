import com.abtasty.flagship.api.HttpHelper;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.hits.*;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
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
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class, HttpHelper.class })
public class FlagshipIntegrationTests {

    private HashMap<String, OnRequestValidation>  requestToVerify             = new HashMap<>();
    private HashMap<String, HttpURLConnection>    responseToMock              = new HashMap<>();
    private Boolean                                     requestsVerified            = true;
    private Boolean                                     missingRequestVerification  = false;

    public FlagshipIntegrationTests() {
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
                            e.printStackTrace();
                        }
                    } else {
                        missingRequestVerification = true;
                        System.err.println("Error url not verified : " + response.requestUrl);
                    }
                    return response;
                }
            }).when(HttpHelper.class, "parseResponse", null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

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
        for (Map.Entry<String, HttpURLConnection> entry : responseToMock.entrySet()) {
            entry.getValue().disconnect();
        }
        responseToMock.clear();
        requestToVerify.clear();

//        try {
//            spy(HttpHelper.class);
//            doAnswer(new Answer<Object>() {
//                @Override
//                public Object answer(InvocationOnMock invocation) throws Throwable {
//                    URL url = invocation.getArgument(0);
//                    if (responseToMock.containsKey(url.toString()))
//                        return responseToMock.get(url.toString());
//                    else
//                        return invocation.callRealMethod();
//                }
//            }).when(HttpHelper.class, "createConnection", any());
//
//            doAnswer(new Answer<Object>() {
//                @Override
//                public Object answer(InvocationOnMock invocation) throws Throwable {
//                    Response response = (Response) invocation.callRealMethod();
//                    if (requestToVerify.containsKey(response.requestUrl)) {
//                        try {
//                            requestToVerify.get(response.getRequestUrl()).onRequestValidation(response);
//                        } catch (Error | Exception e) {
//                            requestsVerified = false;
//                            System.err.println("Error verifying : " + response.requestUrl);
//                            e.printStackTrace();
//                        }
//                    } else {
//                        missingRequestVerification = true;
//                        System.err.println("Error url not verified : " + response.requestUrl);
//                    }
//                    return response;
//                }
//            }).when(HttpHelper.class, "parseResponse", any(), any(), any(), any(), any());
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail();
//        }
    }

    @After
    public void after() {
        assertTrue(requestsVerified);
        assertFalse(missingRequestVerification);
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void config() {

        Flagship.start(null, null, null);
        assertFalse(Flagship.isReady());

        Flagship.start("my_env_id", "my_api_key");
        assertNotNull(Flagship.getConfig());
        assertTrue(Flagship.isReady());
        assertEquals(Flagship.getConfig().getEnvId(), "my_env_id");
        assertEquals(Flagship.getConfig().getApiKey(), "my_api_key");

        CountDownLatch logLatch = new CountDownLatch(1);
        class CustomLogManager extends LogManager {

            public CustomLogManager(Flagship.Log logMode) {
                super(logMode);
            }

            @Override
            public void onLog(Tag tag, LogLevel level, String message) {
                if (message.equals("Flagship SDK started.") && tag == Tag.INITIALIZATION && level == LogLevel.INFO)
                    logLatch.countDown();
            }
        }
        Flagship.start("my_env_id_2", "my_api_key_2", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.DECISION_API)
                .withLogManager(new CustomLogManager(Flagship.Log.ALL)));
        assertNotNull(Flagship.getConfig());
        assertTrue(Flagship.isReady());
        assertEquals(Flagship.getConfig().getEnvId(), "my_env_id_2");
        assertEquals(Flagship.getConfig().getApiKey(), "my_api_key_2");
        assertTrue(Flagship.getConfig().getLogManager() instanceof CustomLogManager);
        try {
            if (!logLatch.await(1, TimeUnit.SECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateContext() {

        CountDownLatch logLatch = new CountDownLatch(2);
        class CustomLogManager extends LogManager {

            public CustomLogManager(Flagship.Log logMode) {
                super(logMode);
            }

            @Override
            public void onLog(Tag tag, LogLevel level, String message) {
                if (level == LogLevel.WARNING)
                    logLatch.countDown();
            }
        }

        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.DECISION_API)
                .withLogManager(new CustomLogManager(Flagship.Log.ALL)));

        Visitor visitor0 = Flagship.newVisitor(null);
        assertNull(visitor0);

        visitor0 = Flagship.newVisitor("visitor_0", null);
        assertNotNull(visitor0);
        assertEquals(visitor0.getContext().size(), 0);

        Visitor finalVisitor = visitor0;
        Visitor visitor1 = Flagship.newVisitor("visitor_1",  new HashMap<String, Object>() {{
            put("boolean", true);
            put("int", 32);
            put("float", 3.14);
            put("double", 3.14d);
            put("String", "String");
            put("json_object", new JSONObject("{\"key\":\"value\"}"));
            put("json_array", new JSONArray("[\"key\",\"value\"]"));
            put("wrong", finalVisitor);
        }});
        assertNotNull(visitor1);
        visitor1.updateContext("wrong2", new Response(0, "", "", null));
        visitor1.updateContext("key1", "value1");
        visitor1.updateContext("key2", 2);
        HashMap<String, Object> context = visitor1.getContext();
        assertEquals(context.size(), 9);
        assertEquals(context.get("boolean"), true);
        assertEquals(context.get("int"), 32);
        assertEquals(context.get("float"), 3.14);
        assertEquals(context.get("double"), 3.14d);
        assertEquals(context.get("String"), "String");
        assertEquals(context.get("key1"), "value1");
        assertEquals(context.get("key2"), 2);
        assertNull(context.get("wrong2"));
        JSONObject json_object = (JSONObject) context.get("json_object");
        JSONArray json_array = (JSONArray) context.get("json_array");
        assertEquals(json_object.get("key"), "value");
        assertEquals(json_array.get(0), "key");

        try {
            if (!logLatch.await(1, TimeUnit.SECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void synchronize() {

        try {
            mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationConstants.synchronizeResponse);

            verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
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
            assertTrue(Flagship.isReady());
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

    @Test
    public void activate() {

        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationConstants.synchronizeResponse2);
        mockResponse("https://decision.flagship.io/v2/activate", 200, "");

        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
        });

        CountDownLatch nbHit = new CountDownLatch(2);
        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            JSONObject content = new JSONObject(request.requestContent);
            assertEquals(content.getString("vid"), "visitor_1");
            assertEquals(content.getString("cid"), "my_env_id");
            if (content.getString("vaid").contains("xxxxxx65k9h02cuc1ae0") &&
                    content.getString("caid").contains("xxxxxxjh6h101fk8lbsg")) {
                nbHit.countDown();
            }
            if (content.getString("vaid").contains("xxxxxx3m649g0h9nkuk0") &&
                    content.getString("caid").contains("xxxxxx3m649g0h9nkuj0")) {
                nbHit.countDown();
            }
        });

        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1", new HashMap<String, Object>() {{
            put("isVIPUser", true);
            put("age", 32);
            put("daysSinceLastLaunch", 2);
        }});

        try {
            CountDownLatch synchronizeLatch = new CountDownLatch(1);
            visitor.synchronizeModifications(() -> {
                assertEquals(visitor.getModification("isref", "default", true), "not a all");
                assertEquals(visitor.getModification("release", 0), 100);
                assertEquals(visitor.getModification("target", "default", true), "default");
                visitor.activateModification("release");
                visitor.activateModification("wrong");
                synchronizeLatch.countDown();
            });
            if (!synchronizeLatch.await(1, TimeUnit.SECONDS) && !nbHit.await(1, TimeUnit.SECONDS))
                fail();
//            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void hits() {

        CountDownLatch nbHit = new CountDownLatch(5);

        mockResponse("https://ariane.abtasty.com", 200, "");

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            assertEquals(request.getType().toString(), "POST");
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "visitor_1");
            assertEquals(content.getString("ds"), "APP");
            assertEquals(content.get("cid"), "my_env_id");
            assertTrue(content.has("t"));
            switch (content.get("t").toString()) {
                case ("SCREENVIEW"): {
                    assertEquals(content.get("uip"), "127.0.0.1");
                    assertEquals(content.get("dl"), "screen location");
                    assertEquals(content.get("sr"), "200x100");
                    assertEquals(content.get("ul"), "fr_FR");
                    assertEquals(content.getInt("sn"), 2);
                    nbHit.countDown();
                    break;
                }
                case ("PAGEVIEW"): {
                    assertEquals(content.get("dl"), "https://location.com");
                    nbHit.countDown();
                    break;
                }
                case ("EVENT"): {
                    assertEquals(content.get("el"), "label");
                    assertEquals(content.get("ea"), "action");
                    assertEquals(content.get("ec"), "User Engagement");
                    assertEquals(content.getInt("ev"), 100);
                    nbHit.countDown();
                    break;
                }
                case ("TRANSACTION"): {
                    assertEquals(content.get("icn"), 1);
                    assertEquals(content.getDouble("tt"), 19.99);
                    assertEquals(content.getDouble("tr"), 199.99);
                    assertEquals(content.getDouble("ts"), 9.99);
                    assertEquals(content.get("tc"), "EUR");
                    assertEquals(content.get("sm"), "1day");
                    assertEquals(content.get("tid"), "#12345");
                    assertEquals(content.get("ta"), "affiliation");
                    assertEquals(content.get("tcc"), "code");
                    assertEquals(content.get("pm"), "creditcard");
                    nbHit.countDown();
                    break;
                }
                case ("ITEM"): {
                    assertEquals(content.getInt("iq"), 1);
                    assertEquals(content.get("tid"), "#12345");
                    assertEquals(content.getDouble("ip"), 199.99);
                    assertEquals(content.get("iv"), "test");
                    assertEquals(content.get("in"), "product");
                    assertEquals(content.get("ic"), "sku123");
                    nbHit.countDown();
                    break;
                }
            }
        });


        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1");

        Screen screen = new Screen("screen location")
                .withResolution(200, 100)
                .withLocale("fr_FR")
                .withIp("127.0.0.1")
                .withSessionNumber(2);
        Page page = new Page("https://location.com");
        Page page2 = new Page("not a url");
        Event event = new Event(Event.EventCategory.USER_ENGAGEMENT, "action")
                .withEventLabel("label")
                .withEventValue(100);
        Event event2 = new Event(Event.EventCategory.USER_ENGAGEMENT, null)
                .withEventLabel("wrong")
                .withEventValue(100);
        Transaction transaction = new Transaction("#12345", "affiliation")
                .withCouponCode("code")
                .withCurrency("EUR")
                .withItemCount(1)
                .withPaymentMethod("creditcard")
                .withShippingCosts(9.99f)
                .withTaxes(19.99f)
                .withTotalRevenue(199.99f)
                .withShippingMethod("1day");
        Transaction transaction2 = new Transaction(null, "affiliation");
        Item item = new Item("#12345", "product", "sku123")
                .withItemCategory("test")
                .withItemPrice(199.99f)
                .withItemQuantity(1);
        Item item2 = new Item("#12345", null, "sku123");

        visitor.sendHit(screen);
        visitor.sendHit(page);
        visitor.sendHit(page2);
        visitor.sendHit(event);
        visitor.sendHit(event2);
        visitor.sendHit(transaction);
        visitor.sendHit(transaction2);
        visitor.sendHit(item);
        visitor.sendHit(item2);
        try {
            if (!nbHit.await(2, TimeUnit.SECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void panic() throws InterruptedException {
        AtomicBoolean calls = new AtomicBoolean(false);
        CountDownLatch campaignsLatch = new CountDownLatch(1);
        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationConstants.panic);
        mockResponse("https://decision.flagship.io/v2/activate", 200, "");
        mockResponse("https://ariane.abtasty.com", 200, "");

        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
//            callCampaigns.set(true);
            campaignsLatch.countDown();
        });

        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            calls.set(true);
        });

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            calls.set(true);
        });

        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1", new HashMap<String, Object>() {{
            put("isVIPUser", true);
            put("age", 32);
            put("daysSinceLastLaunch", 2);
        }});

        visitor.synchronizeModifications(() -> {

            visitor.updateContext("hello", "world");
            assertFalse(visitor.getContext().containsKey("hello"));
            assertEquals(visitor.getModification("wrong", "wrong", true), "wrong");
            visitor.sendHit(new Screen("Integration Test"));
        });

        Thread.sleep(1000);
        assertFalse(calls.get());
    }
}
