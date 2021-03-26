import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.hits.*;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.utils.FlagshipLogManager;
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
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({URL.class, HttpManager.class})
public class FlagshipIntegrationTests {

    private HashMap<String, OnRequestValidation>            requestToVerify = new HashMap<>();
    private ConcurrentHashMap<String, HttpURLConnection>    responseToMock = new ConcurrentHashMap<>();
    private Boolean                                         requestsVerified = true;
    private Boolean                                         missingRequestVerification = false;



    public FlagshipIntegrationTests() {

        try {
//            spy(HttpManager.class);
            HttpManager manager = mock(HttpManager.class);
//            manager.g
            Field instance = HttpManager.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(instance, manager);
            doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    URL url = invocation.getArgument(0);
                    if (responseToMock.containsKey(url.toString())) {
                       return responseToMock.get(url.toString());
                    } else
                        return invocation.callRealMethod();
                }
            }).when(manager, "createConnection", any());
//            }).when(manager.createConnection(any()));

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
                        requestToVerify.remove(response.requestUrl);
                    } else {
                        missingRequestVerification = true;
                        System.err.println("Error url not verified : " + response.requestUrl);
                    }
                    return response;
                }
            }).when(manager, "parseResponse", any(), any(), any(), any(), any());
//            }).when(HttpManager.class, "parseResponse", any(), any(), any(), any(), any());
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
        responseToMock.clear();
        requestToVerify.clear();
    }

    @After
    public void after() {
        assertTrue(requestsVerified);
        assertFalse(missingRequestVerification);
        for (Map.Entry<String, HttpURLConnection> entry : responseToMock.entrySet()) {
            entry.getValue().disconnect();
        }
//        HttpManager.getInstance().closeExecutor();
    }

    @Test
    public void config() {

        Flagship.start(null, null, null);
        assertFalse(Flagship.isReady());

        Flagship.start("null", "null", new FlagshipConfig().withFlagshipMode(null));
        assertTrue(Flagship.isReady());

        Flagship.start("my_env_id", "my_api_key");
        assertNotNull(Flagship.getConfig());
        assertTrue(Flagship.isReady());
        assertEquals(Flagship.getConfig().getEnvId(), "my_env_id");
        assertEquals(Flagship.getConfig().getApiKey(), "my_api_key");

        CountDownLatch logLatch = new CountDownLatch(1);
        class CustomLogManager extends LogManager {

            @Override
            public void onLog(Level level, String tag, String message) {
                if (message.contains("Flagship SDK") && tag == FlagshipLogManager.Tag.INITIALIZATION.getName() && level == Level.INFO)
                    logLatch.countDown();
            }
        }
        Flagship.start("my_env_id_2", "my_api_key_2", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.DECISION_API)
                .withLogManager(new CustomLogManager()));
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

            public CustomLogManager(LogManager.LogMode mode) {
                super(mode);
            }

            @Override
            public void onLog(Level level, String tag, String message) {
                if (level == Level.WARNING)
                    logLatch.countDown();
            }
        }

        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.DECISION_API)
                .withLogManager(new CustomLogManager(LogManager.LogMode.ALL)));

        Visitor visitor0 = Flagship.newVisitor(null);
        assertNull(visitor0);

        visitor0 = Flagship.newVisitor("visitor_0", null);
        assertNotNull(visitor0);
        assertEquals(visitor0.getContext().size(), 0);

        Visitor finalVisitor = visitor0;
        Visitor visitor1 = Flagship.newVisitor("visitor_1", new HashMap<String, Object>() {{
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
        ConcurrentMap<String, Object> context = visitor1.getContext();
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

    @PrepareForTest(HttpManager.class)
    @Test
    public void synchronize() {

        try {
            mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationConstants.synchronizeResponse);

            verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
                assertTrue(request.getType().toString().equalsIgnoreCase("POST"));
                assertEquals(request.getRequestHeaders().get("x-api-key"), "my_api_key");
                assertEquals(request.getRequestHeaders().get("x-sdk-version"), BuildConfig.flagship_version_name);
                assertEquals(request.getRequestHeaders().get("x-sdk-client"), "java");
                JSONObject content = new JSONObject(request.requestContent);
                assertTrue(content.has("context"));
                assertTrue(content.getJSONObject("context").getBoolean("vip"));
                assertEquals(content.getJSONObject("context").getInt("age"), 32);
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
            if (!synchronizeLatch.await(30, TimeUnit.SECONDS))
                fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void activate() throws InterruptedException {

        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationConstants.synchronizeResponse2);
        mockResponse("https://decision.flagship.io/v2/activate", 200, "");

        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
            assertEquals(request.getRequestHeaders().get("x-sdk-version"), BuildConfig.flagship_version_name);
            assertEquals(request.getRequestHeaders().get("x-sdk-client"), "java");
        });

        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1", new HashMap<String, Object>() {{
            put("isVIPUser", true);
            put("age", 32);
            put("daysSinceLastLaunch", 2);
        }});

        try {
            CountDownLatch synchronizeLatch = new CountDownLatch(1);
            assert visitor != null;
            visitor.synchronizeModifications(synchronizeLatch::countDown);
            if (!synchronizeLatch.await(1, TimeUnit.SECONDS))
                fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        visitor.activateModification("release");

        CountDownLatch nbHit1 = new CountDownLatch(1);

        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            JSONObject content = new JSONObject(request.requestContent);
            assertEquals(content.getString("vid"), "visitor_1");
            assertEquals(content.getString("cid"), "my_env_id");
            if (content.getString("vaid").contains("xxxxxx3m649g0h9nkuk0") &&
                    content.getString("caid").contains("xxxxxx3m649g0h9nkuj0")) {
                nbHit1.countDown();
            }
        });
        if (!nbHit1.await(1, TimeUnit.SECONDS))
            fail();

        assertEquals(visitor.getModification("isref", "default", true), "not a all");

        CountDownLatch nbHit2 = new CountDownLatch(1);
        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            JSONObject content = new JSONObject(request.requestContent);
            assertEquals(content.getString("vid"), "visitor_1");
            assertEquals(content.getString("cid"), "my_env_id");
            if (content.getString("vaid").contains("xxxxxx65k9h02cuc1ae0") &&
                    content.getString("caid").contains("xxxxxxjh6h101fk8lbsg")) {
                nbHit2.countDown();
            }
        });
        if (!nbHit2.await(1, TimeUnit.SECONDS))
            fail();


        visitor.activateModification("wrong");
        AtomicInteger call = new AtomicInteger(0);
        CountDownLatch nbHit3 = new CountDownLatch(1);
        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            nbHit3.countDown();
            call.getAndIncrement();
        });
        nbHit3.await(200, TimeUnit.MILLISECONDS);
        assertEquals(call.get(), 0);
    }

    @Test
    public void screenHit() {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1");

        mockResponse("https://ariane.abtasty.com", 200, "");

        CountDownLatch screenHit = new CountDownLatch(1);
        verifyRequest("https://ariane.abtasty.com", (request) -> {
            assertEquals(request.getType().toString(), "POST");
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "visitor_1");
            assertEquals(content.getString("ds"), "APP");
            assertEquals(content.get("cid"), "my_env_id");
            assertEquals(content.get("t"), "SCREENVIEW");
            assertEquals(content.get("uip"), "127.0.0.1");
            assertEquals(content.get("dl"), "screen location");
            assertEquals(content.get("sr"), "200x100");
            assertEquals(content.get("ul"), "fr_FR");
            assertEquals(content.getInt("sn"), 2);
            screenHit.countDown();
        });

        Screen screen = new Screen("screen location")
                .withResolution(200, 100)
                .withLocale("fr_FR")
                .withIp("127.0.0.1")
                .withSessionNumber(2);
        visitor.sendHit(screen);
        try {
            if (!screenHit.await(1, TimeUnit.SECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void pageHit() {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1");

        /////////////////// TEST PAGE HIT //////////////////
        mockResponse("https://ariane.abtasty.com", 200, "");
        CountDownLatch pageHit = new CountDownLatch(1);

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            assertEquals(request.getType().toString(), "POST");
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "visitor_1");
            assertEquals(content.getString("ds"), "APP");
            assertEquals(content.get("cid"), "my_env_id");
            assertEquals(content.get("t"), "PAGEVIEW");
            assertEquals(content.get("dl"), "https://location.com");
            pageHit.countDown();
        });
        Page page = new Page("https://location.com");
        visitor.sendHit(page);
        try {
            if (!pageHit.await(200, TimeUnit.MILLISECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void eventHit() {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1");

        /////////////////// TEST EVENT HIT //////////////////
        mockResponse("https://ariane.abtasty.com", 200, "");
        CountDownLatch eventHit = new CountDownLatch(1);

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            assertEquals(request.getType().toString(), "POST");
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "visitor_1");
            assertEquals(content.getString("ds"), "APP");
            assertEquals(content.get("cid"), "my_env_id");
            assertEquals(content.get("t"), "EVENT");

            assertEquals(content.get("el"), "label");
            assertEquals(content.get("ea"), "action");
            assertEquals(content.get("ec"), "User Engagement");
            assertEquals(content.getInt("ev"), 100);
            eventHit.countDown();
        });
        Event event = new Event(Event.EventCategory.USER_ENGAGEMENT, "action")
                .withEventLabel("label")
                .withEventValue(100);
        visitor.sendHit(event);
        try {
            if (!eventHit.await(200, TimeUnit.MILLISECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void transactionHit() {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1");

        /////////////////// TEST TRANSACTION HIT //////////////////
        mockResponse("https://ariane.abtasty.com", 200, "");
        CountDownLatch transactionHit = new CountDownLatch(1);

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            assertEquals(request.getType().toString(), "POST");
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "visitor_1");
            assertEquals(content.getString("ds"), "APP");
            assertEquals(content.get("cid"), "my_env_id");
            assertEquals(content.get("t"), "TRANSACTION");

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
            transactionHit.countDown();
        });
        Transaction transaction = new Transaction("#12345", "affiliation")
                .withCouponCode("code")
                .withCurrency("EUR")
                .withItemCount(1)
                .withPaymentMethod("creditcard")
                .withShippingCosts(9.99f)
                .withTaxes(19.99f)
                .withTotalRevenue(199.99f)
                .withShippingMethod("1day");
        visitor.sendHit(transaction);
        try {
            if (!transactionHit.await(200, TimeUnit.MILLISECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void itemHit() {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1");

        /////////////////// TEST ITEM HIT //////////////////
        mockResponse("https://ariane.abtasty.com", 200, "");
        CountDownLatch itemHit = new CountDownLatch(1);

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            assertEquals(request.getType().toString(), "POST");
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "visitor_1");
            assertEquals(content.getString("ds"), "APP");
            assertEquals(content.get("cid"), "my_env_id");
            assertEquals(content.get("t"), "ITEM");

            assertEquals(content.getInt("iq"), 1);
            assertEquals(content.get("tid"), "#12345");
            assertEquals(content.getDouble("ip"), 199.99);
            assertEquals(content.get("iv"), "test");
            assertEquals(content.get("in"), "product");
            assertEquals(content.get("ic"), "sku123");
            itemHit.countDown();
        });
        Item item = new Item("#12345", "product", "sku123")
                .withItemCategory("test")
                .withItemPrice(199.99f)
                .withItemQuantity(1);
        visitor.sendHit(item);
        try {
            if (!itemHit.await(200, TimeUnit.MILLISECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }


    @Test
    public void panic() throws InterruptedException {
        AtomicBoolean calls = new AtomicBoolean(false);
        CountDownLatch campaignCall0 = new CountDownLatch(1);
        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationConstants.panic);
        mockResponse("https://decision.flagship.io/v2/activate", 200, "");
        mockResponse("https://ariane.abtasty.com", 200, "");

        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
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
            campaignCall0.countDown();
        });

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        assertFalse(calls.get());
//
        AtomicInteger campaignCall = new AtomicInteger(0);
        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationConstants.synchronizeResponse2);
        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
            campaignCall.getAndAdd(1);
        });
        visitor.synchronizeModifications(null);
        Thread.sleep(200);
        assertEquals(campaignCall.get(), 1);
    }

    @Test
    public void getModifications() {

        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationConstants.synchronizeResponse2);
        CountDownLatch latch = new CountDownLatch(1);
        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {

        });

        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1", new HashMap<String, Object>() {{
            put("isVIPUser", true);
            put("age", 32);
            put("daysSinceLastLaunch", 2);
        }});
        visitor.synchronizeModifications(() -> {
            latch.countDown();
        });
        try {
            if (!latch.await(1000, TimeUnit.MILLISECONDS))
                fail();
            assertEquals(visitor.getModification("release", 0, false), 100);
            assertEquals(visitor.getModification("featureEnabled", false, false), true);
            assertEquals(visitor.getModification("featureEnabled", "wrong", false), "wrong");
            assertEquals(visitor.getModification("isref", -1, false), -1);
            assertEquals(visitor.getModification("visitorIdColor", "default", false), "#E5B21D");
            assertNull(visitor.getModification(null, null, false));
            assertEquals(visitor.getModification(null, "default", false), "default");
            assertNull(visitor.getModification("null", null, false));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getModificationsInfo() {

        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationConstants.synchronizeResponse2);
        CountDownLatch latch = new CountDownLatch(1);
        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {

        });

        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1", new HashMap<String, Object>() {{
            put("isVIPUser", true);
            put("age", 32);
            put("daysSinceLastLaunch", 2);
        }});
        visitor.synchronizeModifications(() -> {
            latch.countDown();
        });
        try {
            if (!latch.await(1000, TimeUnit.MILLISECONDS))
                fail();
            JSONObject infoRelease = visitor.getModificationInfo("release");
            JSONObject infoColor = visitor.getModificationInfo("visitorIdColor");
            JSONObject infoFeature = visitor.getModificationInfo("featureEnabled");
            JSONObject infoNull = visitor.getModificationInfo(null);
            JSONObject infoWrong = visitor.getModificationInfo("wrong");

            assertNull(infoNull);
            assertNull(infoWrong);
            assertEquals(infoRelease.getString("variationId"), "xxxxxx3m649g0h9nkuk0");
            assertEquals(infoRelease.getString("variationGroupId"), "xxxxxx3m649g0h9nkuj0");
            assertEquals(infoRelease.getString("campaignId"), "xxxxxx3m649g0h9nkui0");
            assertFalse(infoRelease.getBoolean("isReference"));

            assertEquals(infoColor.getString("variationId"), "xxxxxx4jaeg0gm49cn0");
            assertEquals(infoColor.getString("variationGroupId"), "xxxxxx4jaeg0gm49ckg");
            assertEquals(infoColor.getString("campaignId"), "xxxxxx4jaeg0gm49cjg");
            assertFalse(infoColor.getBoolean("isReference"));

            assertEquals(infoFeature.getString("variationId"), "xxxxxxe4jaeg0gi1bhq0");
            assertEquals(infoFeature.getString("variationGroupId"), "xxxxxxe4jaeg0gi1bhpg");
            assertEquals(infoFeature.getString("campaignId"), "xxxxxxe4jaeg0gi1bhog");
            assertFalse(infoFeature.getBoolean("isReference"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
