import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.hits.*;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.visitor.Visitor;
import com.abtasty.flagship.utils.ETargetingComp;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;

@RunWith(PowerMockRunner.class)
@PrepareForTest({URL.class, HttpManager.class})
public class FlagshipIntegrationTests {

    private HashMap<String, OnRequestValidation>            requestToVerify = new HashMap<>();
    private ConcurrentHashMap<String, HttpURLConnection>    responseToMock = new ConcurrentHashMap<>();
    private Boolean                                         requestsVerified = true;
    private Boolean                                         missingRequestVerification = false;

    public FlagshipIntegrationTests() {

        try {
            HttpManager mock = PowerMockito.spy(HttpManager.getInstance());
            Whitebox.setInternalState(HttpManager.class, "instance", mock);
            doAnswer(invocation -> {
                URL url = invocation.getArgument(0);
                if (responseToMock.containsKey(url.toString())) {
                    return responseToMock.get(url.toString());
                } else
                    return invocation.callRealMethod();
            }).when(mock, "createConnection", any());

            doAnswer(invocation -> {
                Response response = (Response) invocation.callRealMethod();
                if (requestToVerify.containsKey(response.getRequestUrl())) {
                    OnRequestValidation validation = requestToVerify.get(response.getRequestUrl());
                    try {
                       validation.onRequestValidation(response);
                    } catch (Error | Exception e) {
                        requestsVerified = false;
                        System.err.println("Error verifying : " + response.getRequestUrl());
                        e.printStackTrace();
                    }
                    if (validation.shouldBeRemoved())
                        requestToVerify.remove(response.getRequestUrl());
                } else {
                    missingRequestVerification = true;
                    System.err.println("Error url not verified : " + response.getRequestUrl());
                }
                return response;
            }).when(mock, "parseResponse", any(), any(), any(), any(), any());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    interface OnRequestValidation {
        default boolean shouldBeRemoved() {return true;}
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

    @BeforeEach
    public void before() {

        requestsVerified = true;
        missingRequestVerification = false;
        responseToMock.clear();
        requestToVerify.clear();
    }

    @AfterEach
    public void after() {
        assertTrue(requestsVerified);
//        assertFalse(missingRequestVerification);
        for (Map.Entry<String, HttpURLConnection> entry : responseToMock.entrySet()) {
            entry.getValue().disconnect();
        }
        resetSingleton();
    }

    public static void resetSingleton() {
        Field instance;
        try {
            instance = Flagship.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void config() {
        Flagship.start(null, null, null);
        assertEquals(Flagship.getStatus(), Flagship.Status.NOT_INITIALIZED);

        Flagship.start("null", "null", new FlagshipConfig().withFlagshipMode(null));
        assertEquals(Flagship.getStatus(), Flagship.Status.READY);

        Flagship.start("my_env_id", "my_api_key");
        assertSame(Flagship.getStatus(), Flagship.Status.READY);
        assertNotNull(Flagship.newVisitor("test"));
        assertNotNull(Flagship.getConfig());
        assertEquals(Flagship.getConfig().getEnvId(), "my_env_id");
        assertEquals(Flagship.getConfig().getApiKey(), "my_api_key");

        CountDownLatch logLatch = new CountDownLatch(1);
        class CustomLogManager extends LogManager {

            @Override
            public void onLog(Level level, String tag, String message) {
                if (message.contains("Flagship SDK") && tag.equals(FlagshipLogManager.Tag.GLOBAL.getName()) && level == Level.INFO)
                    logLatch.countDown();
            }
        }
        Flagship.start("my_env_id_2", "my_api_key_2", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.DECISION_API)
                .withLogManager(new CustomLogManager()));
        assertNotNull(Flagship.getConfig());
        assertEquals(Flagship.getStatus(), Flagship.Status.READY);
        assertEquals(Flagship.getConfig().getEnvId(), "my_env_id_2");
        assertEquals(Flagship.getConfig().getApiKey(), "my_api_key_2");
        assertTrue(Flagship.getConfig().getLogManager() instanceof CustomLogManager);
        try {
            if (!logLatch.await(2, TimeUnit.SECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateContext() {

        CountDownLatch logLatch = new CountDownLatch(2);
        class CustomLogManager extends LogManager {

            public CustomLogManager(LogManager.Level level) {
                super(level);
            }

            @Override
            public void onLog(Level level, String tag, String message) {
                if (level == Level.WARNING)
                    logLatch.countDown();
            }
        }

        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.DECISION_API)
                .withLogManager(new CustomLogManager(LogManager.Level.ALL)));

        Visitor visitor0 = Flagship.newVisitor(null);
        assertNotNull(visitor0);

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
                JSONObject content = new JSONObject(request.getRequestContent());
                assertTrue(content.has("context"));
                assertTrue(content.getJSONObject("context").getBoolean("vip"));
                assertEquals(content.getJSONObject("context").getInt("age"), 32);
                assertEquals(content.get("visitorId"), "visitor_1");
            });

            CountDownLatch synchronizeLatch = new CountDownLatch(1);
            Flagship.start("my_env_id", "my_api_key");
            assertEquals(Flagship.getStatus(), Flagship.Status.READY);
            Visitor visitor = Flagship.newVisitor("visitor_1", new HashMap<String, Object>() {{
                put("vip", true);
                put("age", 32);
            }});
            assert visitor != null;
            visitor.synchronizeModifications().whenComplete((Void, error) -> {
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
            visitor.synchronizeModifications().whenComplete((Void, error) -> {
                synchronizeLatch.countDown();
            });
            if (!synchronizeLatch.await(1, TimeUnit.SECONDS))
                fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        visitor.activateModification("release");

        CountDownLatch nbHit1 = new CountDownLatch(1);

        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
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
            JSONObject content = new JSONObject(request.getRequestContent());
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
        visitor.synchronizeModifications().whenComplete((Void, error) -> {
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
        visitor.synchronizeModifications();
        Thread.sleep(200);
        assertEquals(1, campaignCall.get());
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
        visitor.synchronizeModifications().whenComplete((Void, error) -> {
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
        visitor.synchronizeModifications().whenComplete((Void, error) -> {
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

    @Test
    public void targetingCompare() {
        try {
            assertTrue(ETargetingComp.get("EQUALS").compare("test", "test"));
            assertFalse(ETargetingComp.get("EQUALS").compare("test", "tests"));
            assertFalse(ETargetingComp.get("EQUALS").compare("test", 1));
            assertTrue(ETargetingComp.get("EQUALS").compare(1.0f, 1));
            assertTrue(ETargetingComp.get("EQUALS").compare(1.0f, 1.0d));
            assertTrue(ETargetingComp.get("EQUALS").compare("A", new JSONArray("['B', 'A']")));

            assertFalse(ETargetingComp.get("NOT_EQUALS").compare("test", "test"));
            assertTrue(ETargetingComp.get("NOT_EQUALS").compare("test", "tests"));
            assertFalse(ETargetingComp.get("NOT_EQUALS").compare("test", 1));
            assertFalse(ETargetingComp.get("NOT_EQUALS").compare("test", 1));
            assertTrue(ETargetingComp.get("NOT_EQUALS").compare(1.1, 1));
            assertTrue(ETargetingComp.get("NOT_EQUALS").compare("A", new JSONArray("['B', 'C']")));

            assertTrue(ETargetingComp.get("CONTAINS").compare("test", "test"));
            assertFalse(ETargetingComp.get("CONTAINS").compare("test", "tests"));
            assertTrue(ETargetingComp.get("CONTAINS").compare("tests", "test"));
            assertFalse(ETargetingComp.get("CONTAINS").compare("test", 1));
            assertTrue(ETargetingComp.get("CONTAINS").compare(1.1, 1));
            assertTrue(ETargetingComp.get("CONTAINS").compare("Aaa", new JSONArray("['B', 'C', 'A']")));

            assertFalse(ETargetingComp.get("NOT_CONTAINS").compare("test", "test"));
            assertTrue(ETargetingComp.get("NOT_CONTAINS").compare("test", "tests"));
            assertFalse(ETargetingComp.get("NOT_CONTAINS").compare("test", "test"));
            assertFalse(ETargetingComp.get("NOT_CONTAINS").compare("test", 1));
            assertFalse(ETargetingComp.get("NOT_CONTAINS").compare(1.1, 1));
            assertTrue(ETargetingComp.get("NOT_CONTAINS").compare("aaa", new JSONArray("['B', 'C', 'A']")));

            assertFalse(ETargetingComp.get("GREATER_THAN").compare("test", "test"));
            assertTrue(ETargetingComp.get("GREATER_THAN").compare("test", "TEST"));
            assertFalse(ETargetingComp.get("GREATER_THAN").compare("TEST", "TEST"));
            assertFalse(ETargetingComp.get("GREATER_THAN").compare("TEST", "test"));
            assertFalse(ETargetingComp.get("GREATER_THAN").compare("TEST", false));
            assertFalse(ETargetingComp.get("GREATER_THAN").compare(5,  new JSONArray("[3, 2, 1]")));
            assertFalse(ETargetingComp.get("GREATER_THAN").compare(false, false));
            assertFalse(ETargetingComp.get("GREATER_THAN").compare(false, true));
            assertTrue(ETargetingComp.get("GREATER_THAN").compare(true, false));
            assertFalse(ETargetingComp.get("GREATER_THAN").compare(2f, 8.0d));


            assertFalse(ETargetingComp.get("LOWER_THAN").compare("test", "test"));
            assertFalse(ETargetingComp.get("LOWER_THAN").compare("test", "TEST"));
            assertFalse(ETargetingComp.get("LOWER_THAN").compare("TEST", "TEST"));
            assertTrue(ETargetingComp.get("LOWER_THAN").compare("TEST", "test"));
            assertFalse(ETargetingComp.get("LOWER_THAN").compare("TEST", false));
            assertFalse(ETargetingComp.get("LOWER_THAN").compare(5,  new JSONArray("[3, 2, 1]")));
            assertFalse(ETargetingComp.get("LOWER_THAN").compare(false, false));
            assertTrue(ETargetingComp.get("LOWER_THAN").compare(false, true));
            assertFalse(ETargetingComp.get("LOWER_THAN").compare(true, false));
            assertTrue(ETargetingComp.get("LOWER_THAN").compare(2f, 8.0d));

            assertTrue(ETargetingComp.get("GREATER_THAN_OR_EQUALS").compare("test", "test"));
            assertTrue(ETargetingComp.get("GREATER_THAN_OR_EQUALS").compare(4, 4.0));
            assertTrue(ETargetingComp.get("GREATER_THAN_OR_EQUALS").compare(4, 2.0));
            assertFalse(ETargetingComp.get("GREATER_THAN_OR_EQUALS").compare(false, 2.0));

            assertTrue(ETargetingComp.get("LOWER_THAN_OR_EQUALS").compare("test", "test"));
            assertTrue(ETargetingComp.get("LOWER_THAN_OR_EQUALS").compare(4, 4.0));
            assertFalse(ETargetingComp.get("LOWER_THAN_OR_EQUALS").compare(4, 2.0));
            assertFalse(ETargetingComp.get("LOWER_THAN_OR_EQUALS").compare(false, 2.0));

            assertTrue(ETargetingComp.get("STARTS_WITH").compare("test", "test"));
            assertTrue(ETargetingComp.get("STARTS_WITH").compare("testa", "test"));
            assertTrue(ETargetingComp.get("ENDS_WITH").compare("test", "test"));
            assertTrue(ETargetingComp.get("ENDS_WITH").compare("atest", "test"));

        } catch (Exception e) {
            assert false;
        }
    }


    @Test
    public void bucketingPollingInterval() throws InterruptedException, ExecutionException {
        mockResponse("https://cdn.flagship.io/my_env_id/bucketing.json", 200, FlagshipIntegrationConstants.bucketingResponse);
        AtomicInteger nbBucketingCall = new AtomicInteger(0);
        verifyRequest("https://cdn.flagship.io/my_env_id/bucketing.json", new OnRequestValidation() {

            @Override
            public boolean shouldBeRemoved() {
                return false;
            }

            @Override
            public void onRequestValidation(Response response) {
                    nbBucketingCall.incrementAndGet();
            }
        });
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.BUCKETING)
                .withBucketingPollingIntervals(8, TimeUnit.SECONDS));
        Thread.sleep(10000);
        assertEquals(2, nbBucketingCall.get());
        nbBucketingCall.set(0);
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.BUCKETING)
                .withBucketingPollingIntervals(2, TimeUnit.SECONDS));
        Thread.sleep(10500);
        assertEquals(7, nbBucketingCall.get());
    }

    @Test
    public void bucketing() throws InterruptedException, ExecutionException {
        mockResponse("https://cdn.flagship.io/my_env_id/bucketing.json", 200, FlagshipIntegrationConstants.bucketingResponse);
        verifyRequest("https://cdn.flagship.io/my_env_id/bucketing.json", new OnRequestValidation() {
            @Override
            public boolean shouldBeRemoved() {
                return false;
            }

            @Override
            public void onRequestValidation(Response response) {
            }
        });
        CountDownLatch readyLatch = new CountDownLatch(1);
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.BUCKETING)
                .withBucketingPollingIntervals(1, TimeUnit.MINUTES)
                .withStatusListener(newStatus -> {
                    if (newStatus == Flagship.Status.READY)
                        readyLatch.countDown();
                }));
        if (!readyLatch.await(1, TimeUnit.SECONDS))
            fail();
        Visitor visitor1 = Flagship.newVisitor("visitor_1", new HashMap<String, Object>() {{
            put("ab10_enabled", true);
        }});
        Visitor visitor2 = Flagship.newVisitor("visitor_2", new HashMap<String, Object>() {{
            put("ab10_enabled", true);
        }});
        visitor1.synchronizeModifications().get();
        assertEquals(9, visitor1.getModification("ab10_variation", 0));
        assertEquals("xxxxxxc3fk9jdb020ukg", visitor1.getModificationInfo("ab10_variation").getString("campaignId"));
        assertEquals("xxxxxxc3fk9jdb020ulg", visitor1.getModificationInfo("ab10_variation").getString("variationGroupId"));
        assertEquals("xxxxxxgbcahim831l72g", visitor1.getModificationInfo("ab10_variation").getString("variationId"));
        assertFalse(visitor1.getModificationInfo("ab10_variation").getBoolean("isReference"));
        visitor1.updateContext("ab10_enabled", false);
        visitor1.synchronizeModifications().get();
        assertEquals(0, visitor1.getModification("ab10_variation", 0));
        assertNull(visitor1.getModificationInfo("ab10_variation"));
        visitor2.synchronizeModifications().get();
        assertEquals(2, visitor2.getModification("ab10_variation", 0));
        assertEquals(0, visitor1.getModification("ab10_variation", 0));
    }

    @Test
    public void visitor_status_strategy() throws InterruptedException, ExecutionException {

        CountDownLatch contextLatch = new CountDownLatch(4);
        mockResponse("https://cdn.flagship.io/my_env_id/bucketing.json", 200, FlagshipIntegrationConstants.bucketingResponse);
        verifyRequest("https://cdn.flagship.io/my_env_id/bucketing.json", new OnRequestValidation() {
            @Override
            public boolean shouldBeRemoved() {
                return false;
            }

            @Override
            public void onRequestValidation(Response response) {
            }
        });
        verifyRequest("https://decision.flagship.io/v2/my_env_id/events", new OnRequestValidation() {
            @Override
            public void onRequestValidation(Response response) {
                JSONObject json = response.getRequestContentAsJson();
                assert json.getString("type").equals("CONTEXT");
                assert json.getString("visitorId").equals("visitor_1");
                assert json.getJSONObject("data").getInt("age") == 32;
                contextLatch.countDown();
            }
        });
        CountDownLatch latchNotInitialized = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(1);
        CountDownLatch synchronizeDeactivatedLog = new CountDownLatch(1);
        CountDownLatch consentDeactivatedLog = new CountDownLatch(1);

        assertEquals(Flagship.Status.NOT_INITIALIZED, Flagship.getStatus());
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig()
                .withFlagshipMode(Flagship.Mode.BUCKETING)
                .withBucketingPollingIntervals(1, TimeUnit.MINUTES)
                .withStatusListener(newStatus -> {
                    if (newStatus == Flagship.Status.READY)
                        readyLatch.countDown();
                })
                .withLogManager(new LogManager() {
                    @Override
                    public void onLog(Level level, String tag, String message) {
                        System.out.println("message => " + message);
                        if (message.contains("has been created while SDK status is POLLING"))
                            latchNotInitialized.countDown();
                        else if (message.contains("'synchronizeModifications()' is deactivated"))
                            synchronizeDeactivatedLog.countDown();
                        else if (message.contains("'visitor_1': visitor did not consent."))
                            consentDeactivatedLog.countDown();
                    }
                }));
        assertEquals(Flagship.getStatus(), Flagship.Status.POLLING);
        Visitor visitor_1 = Flagship.newVisitor("visitor_1");
        visitor_1.updateContext("age", 32);
        visitor_1.synchronizeModifications().get();
        assertEquals(0, latchNotInitialized.getCount());
        assertEquals(0, synchronizeDeactivatedLog.getCount());
        readyLatch.await();
        visitor_1.setConsent(false);
        visitor_1.activateModification("key");
        visitor_1.synchronizeModifications().get();
        assertEquals(0, consentDeactivatedLog.getCount());
        visitor_1.setConsent(true);
        visitor_1.synchronizeModifications().get();
        Thread.sleep(1000);
        assertEquals(3, contextLatch.getCount());
    }
}
