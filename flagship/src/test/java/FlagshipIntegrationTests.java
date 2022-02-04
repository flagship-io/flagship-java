import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.cache.CacheHelper;
import com.abtasty.flagship.cache.CacheManager;
import com.abtasty.flagship.cache.IHitCacheImplementation;
import com.abtasty.flagship.cache.IVisitorCacheImplementation;
import com.abtasty.flagship.database.SQLiteCacheManager;
import com.abtasty.flagship.hits.*;
import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Flag;
import com.abtasty.flagship.utils.ETargetingComp;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.Visitor;
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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private HashMap<String, List<Response>>                 interceptedRequests = new HashMap<>();

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
                //v2
                if (interceptedRequests.containsKey(response.getRequestUrl())) {
                    interceptedRequests.get(response.getRequestUrl()).add(response);
                } else {
                    interceptedRequests.put(response.getRequestUrl(), new ArrayList<>(Arrays.asList(response)));
                }
                //
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

        //v2
        interceptedRequests.clear();
    }

    @AfterEach
    public void after() {
        assertTrue(requestsVerified);
//        assertFalse(missingRequestVerification);
        for (Map.Entry<String, HttpURLConnection> entry : responseToMock.entrySet()) {
            entry.getValue().disconnect();
        }
        resetSingleton();
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //v2
        interceptedRequests.clear();
    }

    public static void resetSingleton() {
        Field instance;
        try {
            instance = Flagship.class.getDeclaredField("instance");
            instance.setAccessible(true);

            Field config = Flagship.instance().getClass().getDeclaredField("configManager");
            config.setAccessible(true);
            config.set(Flagship.instance(), new ConfigManager());

            Field status = Flagship.instance().getClass().getDeclaredField("status");
            status.setAccessible(true);
            status.set(Flagship.instance(), Flagship.Status.NOT_INITIALIZED);

            Field visitorInstance = Flagship.instance().getClass().getDeclaredField("singleVisitorInstance");
            visitorInstance.setAccessible(true);
            visitorInstance.set(Flagship.instance(), null);

            instance.set(null, null);

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void config() {
        Flagship.start(null, null, null);
        assertEquals(Flagship.getStatus(), Flagship.Status.NOT_INITIALIZED);

        Flagship.start("null", "null", new FlagshipConfig.DecisionApi());
        assertEquals(Flagship.getStatus(), Flagship.Status.READY);

        Flagship.start("my_env_id", "my_api_key");
        assertSame(Flagship.getStatus(), Flagship.Status.READY);
        assertNotNull(Flagship.newVisitor("test").build());
//        assertNotNull(Flagship.newVisitor("test"));
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
        Flagship.start("my_env_id_2", "my_api_key_2", new FlagshipConfig.DecisionApi()
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

        CountDownLatch logLatch = new CountDownLatch(4);
        class CustomLogManager extends LogManager {

            public CustomLogManager(LogManager.Level level) {
                super(level);
            }

            @Override
            public void onLog(Level level, String tag, String message) {
                if (level == Level.ERROR)
                    logLatch.countDown();
            }
        }

        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig.DecisionApi()
                .withLogManager(new CustomLogManager(LogManager.Level.ALL)));

        Visitor visitor0 = Flagship.newVisitor(null).build();
        assertNotNull(visitor0);

//        visitor0 = Flagship.newVisitor("visitor_0", false, null);
        visitor0 = Flagship.newVisitor("visitor_0")
                .isAuthenticated(false)
                .context(null)
                .build();
        HashMap<String, Object> context0 = visitor0.getContext();
        assertNotNull(visitor0);
        assertEquals(3, context0.size());
        assertEquals(context0.get("fs_client"), "java");
        assertEquals(context0.get("fs_version"), BuildConfig.flagship_version_name);
        assertEquals(context0.get("fs_users"), "visitor_0");

        Visitor finalVisitor = visitor0;
//        Visitor visitor1 = Flagship.newVisitor("visitor_1", false, new HashMap<String, Object>() {{
//            put("boolean", true);
//            put("int", 32);
//            put("float", 3.14);
//            put("double", 3.14d);
//            put("String", "String");
//            put("json_object", new JSONObject("{\"key\":\"value\"}"));
//            put("json_array", new JSONArray("[\"key\",\"value\"]"));
//            put("wrong", finalVisitor);
//        }});
        Visitor visitor1 = Flagship.newVisitor("visitor_1")
                .isAuthenticated(false)
                .context(new HashMap<String, Object>() {{
                    put("boolean", true);
                    put("int", 32);
                    put("float", 3.14);
                    put("double", 3.14d);
                    put("String", "String");
                    put("json_object", new JSONObject("{\"key\":\"value\"}"));
                    put("json_array", new JSONArray("[\"key\",\"value\"]"));
                    put("wrong", finalVisitor);
                }}).build();
        assertNotNull(visitor1);
        visitor1.updateContext("wrong2", new Response(0, "", "", null));
        visitor1.updateContext("key1", "value1");
        visitor1.updateContext("key2", 2);
        HashMap<String, Object> context = visitor1.getContext();
        assertEquals(context.size(), 10);
        assertEquals(context.get("boolean"), true);
        assertEquals(context.get("int"), 32);
        assertEquals(context.get("float"), 3.14);
        assertEquals(context.get("double"), 3.14d);
        assertEquals(context.get("String"), "String");
        assertEquals(context.get("key1"), "value1");
        assertEquals(context.get("key2"), 2);
        assertNull(context.get("wrong2"));
        JSONObject json_object = (JSONObject) context.getOrDefault("json_object", null);
        JSONArray json_array = (JSONArray) context.getOrDefault("json_array", null);
//        assertEquals(json_object.get("key"), "value");
//        assertEquals(json_array.get(0), "key");
        assertNull(json_object);
        assertNull(json_array);

        try {
            if (!logLatch.await(2, TimeUnit.SECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PrepareForTest(HttpManager.class)
    @Test
    public void synchronize() {

        try {
            mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationHelper.synchronizeResponse);

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

            Visitor visitor = Flagship.newVisitor("visitor_1")
                    .context(new HashMap<String, Object>() {{
                        put("vip", true);
                        put("age", 32);
                    }}).build();
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

        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationHelper.synchronizeResponse2);
        mockResponse("https://decision.flagship.io/v2/activate", 200, "");

        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
            assertEquals(request.getRequestHeaders().get("x-sdk-version"), BuildConfig.flagship_version_name);
            assertEquals(request.getRequestHeaders().get("x-sdk-client"), "java");
        });

        Flagship.start("my_env_id", "my_api_key");

        Visitor visitor = Flagship.newVisitor("visitor_1")
                .context(new HashMap<String, Object>() {{
                    put("isVIPUser", true);
                    put("age", 32);
                    put("daysSinceLastLaunch", 2);
                }}).build();

        try {
            CountDownLatch synchronizeLatch = new CountDownLatch(1);
            assert visitor != null;
            visitor.synchronizeModifications().whenComplete((Void, error) -> {
                synchronizeLatch.countDown();
            });
            if (!synchronizeLatch.await(2, TimeUnit.SECONDS))
                fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

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

        visitor.activateModification("release");

        if (!nbHit1.await(2, TimeUnit.SECONDS))
            fail();

        CountDownLatch nbHit2 = new CountDownLatch(1);
        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
//            if (!request.getRequestContent().contains("fs_consent")) { // prevent consent hit
                JSONObject content = new JSONObject(request.getRequestContent());
                assertEquals(content.getString("vid"), "visitor_1");
                assertEquals(content.getString("cid"), "my_env_id");
                if (content.getString("vaid").contains("xxxxxx65k9h02cuc1ae0") &&
                        content.getString("caid").contains("xxxxxxjh6h101fk8lbsg")) {
                    nbHit2.countDown();
                }
//            }
        });

        Thread.sleep(200);
        assertEquals(visitor.getModification("isref", "default", true), "not a all");

        if (!nbHit2.await(3, TimeUnit.SECONDS))
            fail();


        AtomicInteger call = new AtomicInteger(0);
        CountDownLatch nbHit3 = new CountDownLatch(1);
        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            nbHit3.countDown();
            call.getAndIncrement();
        });
        visitor.activateModification("wrong");
        nbHit3.await(200, TimeUnit.MILLISECONDS);
        assertEquals(call.get(), 0);
    }

    @Test
    public void screenHit() throws InterruptedException {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1").build();

        mockResponse("https://ariane.abtasty.com", 200, "");

        Thread.sleep(200); //wait for consent
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
            if (!screenHit.await(2, TimeUnit.SECONDS))
                fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void pageHit() {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1").build();

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
    public void eventHit() throws InterruptedException {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1").build();

        Thread.sleep(200); // wait for consent
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
    public void transactionHit() throws InterruptedException {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1").build();

        /////////////////// TEST TRANSACTION HIT //////////////////
        mockResponse("https://ariane.abtasty.com", 200, "");
        CountDownLatch transactionHit = new CountDownLatch(1);

        Thread.sleep(200); // wait for consent
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
    public void itemHit() throws InterruptedException {
        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1").build();

        Thread.sleep(200); // wait for consent
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
        CountDownLatch consentCalls = new CountDownLatch(10);
        CountDownLatch campaignCall0 = new CountDownLatch(1);
        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationHelper.panic);
        mockResponse("https://decision.flagship.io/v2/activate", 200, "");
        mockResponse("https://ariane.abtasty.com", 200, "");

        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
        });

        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            calls.set(true);
        });

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            if (request.getRequestContent().contains("fs_consent"))
                consentCalls.countDown();
            else
                calls.set(true);
        });

        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1")
                .context(new HashMap<String, Object>() {{
                    put("isVIPUser", true);
                    put("age", 32);
                    put("daysSinceLastLaunch", 2);
                }}).build();
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
        assertEquals(9, consentCalls.getCount());
//
        AtomicInteger campaignCall = new AtomicInteger(0);
        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationHelper.synchronizeResponse2);
        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
            campaignCall.getAndAdd(1);
        });
        visitor.synchronizeModifications();
        Thread.sleep(20);
        assertEquals(1, campaignCall.get());
    }

    @Test
    public void getModifications() {

        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationHelper.synchronizeResponse2);
        CountDownLatch latch = new CountDownLatch(1);
        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {

        });

        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1")
                .context(new HashMap<String, Object>() {{
                    put("isVIPUser", true);
                    put("age", 32);
                    put("daysSinceLastLaunch", 2);
                }}).build();
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

        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationHelper.synchronizeResponse2);
        CountDownLatch latch = new CountDownLatch(1);
        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {

        });

        Flagship.start("my_env_id", "my_api_key");
        Visitor visitor = Flagship.newVisitor("visitor_1").context(new HashMap<String, Object>() {{
            put("isVIPUser", true);
            put("age", 32);
            put("daysSinceLastLaunch", 2);
        }}).build();
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
        mockResponse("https://cdn.flagship.io/my_env_id/bucketing.json", 200, FlagshipIntegrationHelper.bucketingResponse);
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
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig.Bucketing()
                .withPollingIntervals(8, TimeUnit.SECONDS));
        Thread.sleep(10000);
        assertEquals(2, nbBucketingCall.get());
        nbBucketingCall.set(0);
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig.Bucketing()
                .withPollingIntervals(2, TimeUnit.SECONDS));
        Thread.sleep(10500);
        assertEquals(6, nbBucketingCall.get());
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig.DecisionApi());
    }

    @Test
    public void bucketing() throws InterruptedException, ExecutionException {
        mockResponse("https://cdn.flagship.io/my_env_id/bucketing.json", 200, FlagshipIntegrationHelper.bucketingResponse);
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
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig.Bucketing()
                .withPollingIntervals(1, TimeUnit.MINUTES)
                .withStatusListener(newStatus -> {
                    if (newStatus == Flagship.Status.READY)
                        readyLatch.countDown();
                }));
        if (!readyLatch.await(2, TimeUnit.SECONDS))
            fail();
        Visitor visitor1 = Flagship.newVisitor("visitor_1")
                .context(new HashMap<String, Object>() {{
                    put("ab10_enabled", true);
                }})
                .build();
        Visitor visitor2 = Flagship.newVisitor("visitor_2")
                .context(new HashMap<String, Object>() {{
                    put("ab10_enabled", true);
                }})
                .build();
        visitor1.synchronizeModifications().get();
        System.out.println("=> " + visitor1.getModification("ab10_variation", 0));
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

        Thread.sleep(400);
        CountDownLatch contextLatch = new CountDownLatch(4);
        mockResponse("https://cdn.flagship.io/my_env_id/bucketing.json", 200, FlagshipIntegrationHelper.bucketingResponse);
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
                assert json.getJSONObject("data").getString("fs_users").equals("visitor_1");
                assert json.getJSONObject("data").getString("fs_version").equals(BuildConfig.flagship_version_name);
                assert json.getJSONObject("data").getString("fs_client").equals("java");
                contextLatch.countDown();
            }
        });
        Visitor visitor_1 = Flagship.newVisitor("visitor_1").build(); // ??
        CountDownLatch latchNotInitialized = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(1);
        CountDownLatch synchronizeDeactivatedLog = new CountDownLatch(1);
        CountDownLatch consentDeactivatedLog = new CountDownLatch(1);

        assertEquals(Flagship.Status.NOT_INITIALIZED, Flagship.getStatus());
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig.Bucketing()
                .withPollingIntervals(1, TimeUnit.MINUTES)
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
                        else if (message.contains("deactivated") && message.contains("fetchFlags()"))
                            synchronizeDeactivatedLog.countDown();
                        else if (message.contains("'visitor_1': visitor did not consent."))
                            consentDeactivatedLog.countDown();
                    }
                }));
        assertEquals(Flagship.getStatus(), Flagship.Status.POLLING);
//        Visitor visitor_1 = Flagship.newVisitor("visitor_1").build();
        visitor_1.setConsent(true);
        visitor_1.updateContext("age", 32);
        visitor_1.synchronizeModifications().get();
        assertEquals(1, latchNotInitialized.getCount()); //only consent
        assertEquals(0, synchronizeDeactivatedLog.getCount());
        readyLatch.await();
        visitor_1.setConsent(false);
        visitor_1.activateModification("key");
        visitor_1.synchronizeModifications().get();
        assertEquals(0, consentDeactivatedLog.getCount());
        visitor_1.setConsent(true);
        visitor_1.synchronizeModifications().get();
        boolean toZero = contextLatch.await(3000, TimeUnit.MILLISECONDS);
        assertFalse(toZero);
        assertEquals(3, contextLatch.getCount());
    }

    @Test
    public void authentication() throws InterruptedException, ExecutionException {

        mockResponse("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", 200, FlagshipIntegrationHelper.synchronizeResponse2);
        mockResponse("https://decision.flagship.io/v2/activate", 200, "");
        mockResponse("https://ariane.abtasty.com", 200, "");

        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("visitorId"), "logged_out");
            assertNull(content.optString("anonymousId", null));
        });

        //anonymous
        Flagship.start("my_env_id", "my_api_key", new FlagshipConfig.DecisionApi());
        Visitor visitor = Flagship.newVisitor("logged_out")
                .context(new HashMap<String, Object>() {{
                    put("isVIPUser", true);
                    put("age", 32);
                    put("daysSinceLastLaunch", 2);
                }}).build();
        visitor.synchronizeModifications().get();


        CountDownLatch anonymous_latch = new CountDownLatch(2);
        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "logged_out");
            assertNull(content.optString("aid", null));
            anonymous_latch.countDown();
        });

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "logged_out");
            assertNull(content.optString("cuid", null));
            anonymous_latch.countDown();
        });

        //logged in 1
        CountDownLatch logged1_latch = new CountDownLatch(2);
        visitor.authenticate("logged_in");
        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("anonymousId"), "logged_out");
            assertEquals(content.getString("visitorId"), "logged_in");
        });
        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "logged_in");
            assertEquals(content.getString("aid"), "logged_out");
            logged1_latch.countDown();
        });

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "logged_out");
            assertEquals(content.getString("cuid"), "logged_in");
            logged1_latch.countDown();
        });
        visitor.synchronizeModifications().get();
        visitor.sendHit(new Screen("test"));
        visitor.activateModification("isref");

        if (!logged1_latch.await(2, TimeUnit.SECONDS))
            fail();

        //logged in 2
        visitor.authenticate("logged_in_2");
        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("anonymousId"), "logged_out");
            assertEquals(content.getString("visitorId"), "logged_in_2");
        });

        CountDownLatch logged2_latch = new CountDownLatch(2);
        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "logged_in_2");
            assertEquals(content.getString("aid"), "logged_out");
            logged2_latch.countDown();
        });

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            if (!request.getRequestContent().contains("fs_consent")) { // prevent consent hit
                JSONObject content = new JSONObject(request.getRequestContent());
                assertEquals(content.getString("vid"), "logged_out");
                assertEquals(content.getString("cuid"), "logged_in_2");
                logged2_latch.countDown();
            }
        });

        visitor.synchronizeModifications().get();
        Thread.sleep(200); // wait for consent
        visitor.sendHit(new Screen("test"));
        visitor.activateModification("isref");

        if (!logged2_latch.await(2, TimeUnit.SECONDS))
            fail();

        //back to anonymous
        CountDownLatch anonymous2_latch = new CountDownLatch(2);
        visitor.unauthenticate();
        verifyRequest("https://decision.flagship.io/v2/my_env_id/campaigns/?exposeAllKeys=true", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertNull(content.optString("anonymousId", null));
            assertEquals(content.getString("visitorId"), "logged_out");
        });

        verifyRequest("https://decision.flagship.io/v2/activate", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "logged_out");
            assertNull(content.optString("aid", null));
            anonymous2_latch.countDown();
        });

        verifyRequest("https://ariane.abtasty.com", (request) -> {
            JSONObject content = new JSONObject(request.getRequestContent());
            assertEquals(content.getString("vid"), "logged_out");
            assertNull(content.optString("cuid", null));
            anonymous2_latch.countDown();
        });

        visitor.synchronizeModifications().get();
        visitor.sendHit(new Screen("test"));
        visitor.activateModification("isref");

        if (!anonymous2_latch.await(2, TimeUnit.SECONDS))
            fail();
        visitor.synchronizeModifications().get();
    }

    @Test
    public void visitor_instance() {
        Flagship.start("my_env_id", "my_api_key");

        Flagship.newVisitor("visitor_1");
        assertNull(Flagship.getVisitor());
        Flagship.newVisitor("visitor_1", Visitor.Instance.NEW_INSTANCE);
        assertNull(Flagship.getVisitor());
        Visitor visitor_2 = Flagship.newVisitor("visitor_2", Visitor.Instance.SINGLE_INSTANCE).build();
        assertNotNull(Flagship.getVisitor());
        assertSame(Flagship.getVisitor(), visitor_2);
        assertSame("visitor_2", Flagship.getVisitor().getId());
        Visitor visitor_3 = Flagship.newVisitor("visitor_3", Visitor.Instance.SINGLE_INSTANCE).build();
        assertSame(Flagship.getVisitor(), visitor_3);
        assertSame("visitor_3", Flagship.getVisitor().getId());

        Visitor visitor_4 = Flagship.newVisitor("visitor_4", Visitor.Instance.SINGLE_INSTANCE).build();
        visitor_4.updateContext("color", "blue");
        assertSame("blue", Flagship.getVisitor().getContext().get("color"));

        Visitor visitor_5 = Flagship.newVisitor("visitor_5", Visitor.Instance.SINGLE_INSTANCE).build();
        assertNull(Flagship.getVisitor().getContext().get("color"));

        Flagship.getVisitor().updateContext("color", "red");

        assertSame("blue", visitor_4.getContext().get("color").toString());
        assertSame("red", visitor_5.getContext().get("color").toString());

        assertSame("red", Flagship.getVisitor().getContext().get("color"));
    }

    //v2
    public String responseFromAssets(String fileName) {
        String json = "{}";
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String path = classLoader.getResource(fileName).getPath();
            Stream<String> lines = Files.lines(Paths.get(path));
            return lines.collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public <T extends Object> T getFieldValue(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void test_cache_default() throws InterruptedException, ExecutionException {

        CountDownLatch readyCDL = new CountDownLatch(1);
        mockResponse(String.format(FlagshipIntegrationHelper.BUCKETING_URL, FlagshipIntegrationHelper._ENV_ID_),
                200, responseFromAssets("bucketing_response_1.json"));
        mockResponse(String.format(FlagshipIntegrationHelper.CONTEXT_URL, FlagshipIntegrationHelper._ENV_ID_),
                500, "");

        Flagship.start(FlagshipIntegrationHelper._ENV_ID_, FlagshipIntegrationHelper._API_KEY_, new FlagshipConfig.Bucketing()
                .withStatusListener(newStatus -> {
                    if (newStatus == Flagship.Status.READY)
                        readyCDL.countDown();
                }));
        if (!readyCDL.await(500, TimeUnit.MILLISECONDS))
            fail();
        Visitor visitor = Flagship.newVisitor("visitor_id").build();
        visitor.synchronizeModifications().get();
        ConfigManager config = getFieldValue(getFieldValue(visitor, "delegate"), "configManager");
        assertNotNull(config);
        assertNull(config.getFlagshipConfig().getCacheManager());
    }

    @Test
    public void test_cache_sqlite() throws InterruptedException, ExecutionException, IOException {

        CountDownLatch readyCDL = new CountDownLatch(1);
        mockResponse(String.format(FlagshipIntegrationHelper.BUCKETING_URL, FlagshipIntegrationHelper._ENV_ID_),
                200, responseFromAssets("bucketing_response_1.json"));
        mockResponse(String.format(FlagshipIntegrationHelper.CONTEXT_URL, FlagshipIntegrationHelper._ENV_ID_),
                500, "");
        mockResponse(FlagshipIntegrationHelper.ARIANE_URL, 500, "");

        Flagship.start(FlagshipIntegrationHelper._ENV_ID_, FlagshipIntegrationHelper._API_KEY_, new FlagshipConfig.Bucketing()
                .withStatusListener(newStatus -> {
                    if (newStatus == Flagship.Status.READY)
                        readyCDL.countDown();
                })
                .withCacheManager(new SQLiteCacheManager("database_test")));
        if (!readyCDL.await(1500, TimeUnit.MILLISECONDS))
            fail();
        assertTrue(new File("./database_test/flagship_database.db").exists());
        Visitor visitor = Flagship.newVisitor("visitor_id").build(); //1 consent
        visitor.synchronizeModifications().get(); //1 context
        ConfigManager config = getFieldValue(getFieldValue(visitor, "delegate"), "configManager");
        assertNotNull(config);
        assertNotNull(config.getFlagshipConfig().getCacheManager());

        IVisitorCacheImplementation visitorImpl = config.getFlagshipConfig().getCacheManager().getVisitorCacheImplementation(); //check default visitor cache
        IHitCacheImplementation hitImpl = config.getFlagshipConfig().getCacheManager().getHitCacheImplementation(); //check default visitor hits cache
        assertEquals(200, config.getFlagshipConfig().getCacheManager().getVisitorCacheLookupTimeout());
        assertEquals(200, config.getFlagshipConfig().getCacheManager().getHitCacheLookupTimeout());

        Thread.sleep(500); //wait for visitor to be inserted
        JSONObject cachedVisitor = visitorImpl.lookupVisitor("visitor_id");
        assertEquals(CacheHelper._VISITOR_CACHE_VERSION_, cachedVisitor.get("version"));
        assertTrue(cachedVisitor.getJSONObject("data").getBoolean("consent"));
        assertEquals("visitor_id", cachedVisitor.getJSONObject("data").getString("visitorId"));

        JSONObject cachedVisitor2 = visitorImpl.lookupVisitor("visitor_id2"); //do not exists
        assertEquals(new JSONObject("{}").toString(), cachedVisitor2.toString());

        JSONArray cachedHits = hitImpl.lookupHits("visitor_id");
        assertEquals(2, cachedHits.length());

        Thread.sleep(500);
        visitor.setConsent(false);
        cachedHits = hitImpl.lookupHits("visitor_id");
        assertEquals(0, cachedHits.length());
        Files.deleteIfExists(new File("./database_test/flagship_database.db").toPath());
    }

    @Test
    public void test_cache_custom() throws InterruptedException, ExecutionException {

        mockResponse(String.format(FlagshipIntegrationHelper.BUCKETING_URL, FlagshipIntegrationHelper._ENV_ID_),
                200, responseFromAssets("bucketing_response_1.json"));
        mockResponse(String.format(FlagshipIntegrationHelper.CONTEXT_URL, FlagshipIntegrationHelper._ENV_ID_),
                500, "");
        mockResponse(FlagshipIntegrationHelper.ARIANE_URL, 500, "");
        mockResponse(FlagshipIntegrationHelper.ACTIVATION_URL, 500, "");

        CountDownLatch readyCDL = new CountDownLatch(1);
        CountDownLatch cacheVisitorLatch = new CountDownLatch(10);
        CountDownLatch lookUpVisitorLatch = new CountDownLatch(10);
        CountDownLatch flushVisitorLatch = new CountDownLatch(10);
        CountDownLatch cacheHitLatch = new CountDownLatch(10);
        CountDownLatch lookupHitsLatch = new CountDownLatch(10);
        CountDownLatch flushHitsLatch = new CountDownLatch(10);
        AtomicBoolean cacheVisitorOk = new AtomicBoolean(false);
        AtomicBoolean cacheHitOk = new AtomicBoolean(false);


        Flagship.start(FlagshipIntegrationHelper._ENV_ID_, FlagshipIntegrationHelper._API_KEY_, new FlagshipConfig.Bucketing()
                .withStatusListener(newStatus -> {
                    if (newStatus == Flagship.Status.READY)
                        readyCDL.countDown();
                })
                .withCacheManager(new CacheManager() {

                    @Override
                    public Long getVisitorCacheLookupTimeout() {
                        return 500L;
                    }

                    @Override
                    public Long getHitCacheLookupTimeout() {
                        return 500L;
                    }

                    @Override
                    public TimeUnit getTimeoutUnit() {
                        return TimeUnit.MILLISECONDS;
                    }

                    @Override
                    public IVisitorCacheImplementation getVisitorCacheImplementation() {
                        return new IVisitorCacheImplementation() {
                            @Override
                            public void cacheVisitor(String visitorId, JSONObject data) {
                                cacheVisitorLatch.countDown();
                                if (visitorId.equals("visitor_id")) {
                                    assertEquals("visitor_id", visitorId);
                                    assertEquals(CacheHelper._VISITOR_CACHE_VERSION_, data.get("version"));
                                    assertTrue(data.getJSONObject("data").getBoolean("consent"));
                                    assertEquals("visitor_id", data.getJSONObject("data").getString("visitorId"));
                                    assertEquals("null", data.getJSONObject("data").optString("anonymousId", "null"));
                                    assertTrue(data.getJSONObject("data").getJSONObject("context").getBoolean("vip"));
                                    assertTrue(data.getJSONObject("data").getJSONObject("context").getBoolean("vip"));
                                    assertEquals("java", data.getJSONObject("data").getJSONObject("context").getString("fs_client"));
                                    JSONObject jsonCampaign = data.getJSONObject("data").getJSONArray("campaigns").getJSONObject(0);
                                    assertEquals("brjjpk7734cg0sl5llll", jsonCampaign.getString("campaignId"));
                                    assertEquals("brjjpk7734cg0sl5mmmm", jsonCampaign.getString("variationGroupId"));
                                    assertEquals("brjjpk7734cg0sl5oooo", jsonCampaign.getString("variationId"));
                                    assertFalse(jsonCampaign.getBoolean("isReference"));
                                    assertEquals("ab", jsonCampaign.getString("type"));
                                    assertTrue(jsonCampaign.getBoolean("activated")); //it is loaded from cache with true
                                    JSONObject jsonFlags = jsonCampaign.getJSONObject("flags");
                                    assertEquals(81111, jsonFlags.get("rank"));
                                    assertTrue(jsonFlags.has("rank_plus"));
                                    cacheVisitorOk.set(true);
                                }
                            }

                            @Override
                            public JSONObject lookupVisitor(String visitorId) {
                                if (visitorId.equals("visitor_id_2")) {
                                    try {
                                        Thread.sleep(50000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                lookUpVisitorLatch.countDown();
                                return new JSONObject(responseFromAssets("cache_visitor.json"));
                            }

                            @Override
                            public void flushVisitor(String visitorId) {
                                flushVisitorLatch.countDown();
                            }
                        };
                    }

                    @Override
                    public IHitCacheImplementation getHitCacheImplementation() {
                        return new IHitCacheImplementation() {
                            @Override
                            public void cacheHit(String visitorId, JSONObject data) {
                                if (visitorId.equals("visitor_id")) {
                                    assertEquals("visitor_id", visitorId);
                                    assertEquals(CacheHelper._HIT_CACHE_VERSION_, data.get("version"));
                                    JSONObject jsonData = data.getJSONObject("data");
                                    assertTrue(jsonData.getLong("time") > 0);
                                    assertEquals("visitor_id", data.getJSONObject("data").getString("visitorId"));
                                    assertEquals("null", data.getJSONObject("data").optString("anonymousId", "null"));
                                    String type = jsonData.getString("type");
                                    JSONObject jsonContent = jsonData.getJSONObject("content");
                                    switch (type) {
                                        case "EVENT": {
                                            assertEquals("EVENT", jsonContent.getString("t"));
                                            assertTrue(jsonContent.getString("el").equals("java:true") || jsonContent.getString("el").equals("java:false"));
                                            assertEquals("fs_consent", jsonContent.getString("ea"));
                                            assertEquals("visitor_id", jsonContent.getString("vid"));
                                            cacheHitLatch.countDown();
                                            break;
                                        }
                                        case "CONTEXT": {
                                            assertEquals("visitor_id", jsonContent.getString("visitorId"));
                                            assertEquals("CONTEXT", jsonContent.getString("type"));
                                            JSONObject contextData = jsonContent.getJSONObject("data");
                                            assertEquals("Android", contextData.getString("sdk_osName"));
                                            assertTrue(contextData.getBoolean("vip"));
                                            assertEquals(2, contextData.getInt("daysSinceLastLaunch"));
                                            cacheHitLatch.countDown();
                                            break;
                                        }
                                        case "BATCH": {
                                            assertEquals("visitor_id", jsonContent.getString("vid"));
                                            JSONArray hits = jsonContent.getJSONArray("h");
                                            assertEquals(2, hits.length()); //One hit should be omitted : too old
                                            assertEquals("SCREENVIEW", hits.getJSONObject(0).getString("t"));
                                            assertEquals("Screen_1", hits.getJSONObject(0).getString("dl"));
                                            assertTrue(hits.getJSONObject(0).getLong("qt") > 0);
                                            assertEquals("EVENT", hits.getJSONObject(1).getString("t"));
                                            assertEquals("Event_1", hits.getJSONObject(1).getString("ea"));
                                            assertTrue(hits.getJSONObject(1).getLong("qt") > 0);
                                            cacheHitLatch.countDown();
                                            break;
                                        }
                                        default: {
                                            break;
                                        }
                                    }
                                    cacheHitOk.set(true);

                                } else if (visitorId.equals("visitor_id_2")) {
                                    cacheHitLatch.countDown();
                                }
                            }

                            @Override
                            public JSONArray lookupHits(String visitorId) {
                                JSONArray array;
                                if (visitorId.equals("visitor_id_2"))
                                    array = new JSONArray("{]"); //shouldn't crash
                                else {
                                    array = new JSONArray(responseFromAssets("cache_hit.json"));
                                    array.getJSONObject(0).getJSONObject("data").put("time", System.currentTimeMillis() - 2000);
                                }
                                lookupHitsLatch.countDown();
                                return array;
                            }

                            @Override
                            public void flushHits(String visitorId) {
                                flushHitsLatch.countDown();
                            }
                        };
                    }
                }));
        if (!readyCDL.await(1000, TimeUnit.MILLISECONDS))
            fail();
        Visitor visitor = Flagship.newVisitor("visitor_id") // 1 consent true
                .context(new HashMap<String, Object>() {{
                    put("vip", true);
                    put("access", "password");
                    put("daysSinceLastLaunch", 2);
                }})
                .build();
        visitor.synchronizeModifications().get(); // 1 context
        assertEquals("Ahoy", visitor.getModification("title", "null", true)); // 1 activate

        Thread.sleep(1000);
        visitor.setConsent(false); // 1 consent false hit
        visitor.sendHit(new Page("https://www.page.com"));
        visitor.sendHit(new Screen("ScreenActivity"));

        assertTrue(cacheVisitorOk.get());
        assertTrue(cacheHitOk.get());
        cacheVisitorOk.set(false);
        cacheHitOk.set(false);

        Thread.sleep(500);
        visitor = Flagship.newVisitor("visitor_id") // 1 consent true // + 1 cached
                .context(new HashMap<String, Object>() {{
                    put("vip", true);
                    put("access", "password");
                    put("daysSinceLastLaunch", 2);
                }})
                .build();
        visitor.synchronizeModifications().get(); // 1 context

        Visitor visitor2 = Flagship.newVisitor("visitor_id_2").build(); // 1 consent true

        Thread.sleep(500);

        ConfigManager config = getFieldValue(getFieldValue(visitor, "delegate"), "configManager");
        assertNotNull(config);
        assertNotNull(config.getFlagshipConfig().getCacheManager());

        assertEquals(500, config.getFlagshipConfig().getCacheManager().getVisitorCacheLookupTimeout());
        assertEquals(500, config.getFlagshipConfig().getCacheManager().getHitCacheLookupTimeout());

        assertEquals(8, cacheVisitorLatch.getCount());
        assertEquals(8, lookUpVisitorLatch.getCount());
        assertEquals(9, flushVisitorLatch.getCount());
        assertEquals(2, cacheHitLatch.getCount());
        assertEquals(8, lookupHitsLatch.getCount());
        assertEquals(9, flushHitsLatch.getCount());
        assertTrue(cacheVisitorOk.get());
        assertTrue(cacheHitOk.get());

    }

    @Test
    public void flags() throws InterruptedException, ExecutionException {

        CountDownLatch activateLatch = new CountDownLatch(10);
        CountDownLatch readyLatch = new CountDownLatch(1);

        mockResponse(String.format(FlagshipIntegrationHelper.BUCKETING_URL, FlagshipIntegrationHelper._ENV_ID_),
                200, responseFromAssets("bucketing_response_1.json"));

        mockResponse(FlagshipIntegrationHelper.ARIANE_URL, 500, "");

        Flagship.start(FlagshipIntegrationHelper._ENV_ID_, FlagshipIntegrationHelper._API_KEY_, new FlagshipConfig.Bucketing()
                .withStatusListener(newStatus -> {
                    if (newStatus == Flagship.Status.READY)
                        readyLatch.countDown();
                })
                .withLogManager(new LogManager() {
                    @Override
                    public void onLog(Level level, String tag, String message) {
                        System.out.println("[$tag] : " + message);
                        if (message.contains(FlagshipIntegrationHelper.ACTIVATION_URL))
                            activateLatch.countDown();
                    }


                })
        );
        if (!readyLatch.await(500, TimeUnit.MILLISECONDS))
            fail();
        Visitor visitor = Flagship.newVisitor("visitor_id")
                .context(new HashMap<String, Object>() {{
                    put("vip", true);
                    put("access", "password");
                    put("daysSinceLastLaunch", 2);
                }}).build();
        Flag<Integer> rank = visitor.getFlag("rank", 0);
        assertEquals(0, rank.value(false)); // no activate
        visitor.fetchFlags().get();

        assertEquals(81111, rank.value(true)); // activate
        Flag<String> rank_plus = visitor.getFlag("rank_plus", "a");
        Flag<String> rank_plus2 = visitor.getFlag("rank_plus", null);
        assertEquals("a", rank_plus.value(false));
        assertNull(rank_plus2.value(false));
        assertTrue(rank_plus.exists());
        assertEquals("brjjpk7734cg0sl5llll", rank_plus.metadata().campaignId);
        assertEquals("brjjpk7734cg0sl5mmmm", rank_plus.metadata().variationGroupId);
        assertEquals("brjjpk7734cg0sl5oooo", rank_plus.metadata().variationId);
        assertFalse(rank_plus.metadata().isReference);
        assertEquals("ab", rank_plus.metadata().campaignType);
        assertTrue(rank_plus.metadata().exists());
        assertEquals(5, rank_plus.metadata().toJSON().length());

        Flag<String> do_not_exists = visitor.getFlag("do_not_exists", "a");

        assertEquals("a", do_not_exists.value( false));
        assertFalse(do_not_exists.exists());
        assertEquals("", do_not_exists.metadata().campaignId);
        assertEquals("", do_not_exists.metadata().variationGroupId);
        assertEquals("", do_not_exists.metadata().variationId);
        assertFalse(do_not_exists.metadata().isReference);
        assertEquals("", do_not_exists.metadata().campaignType);
        assertFalse(do_not_exists.metadata().exists());
        assertEquals(0, do_not_exists.metadata().toJSON().length());

        assertEquals("a", visitor.getFlag("rank", "a").value(true)); // no activate
        assertEquals(81111, visitor.getFlag("rank", null).value(true)); // activate

        assertNull(visitor.getFlag("null", null).value(true)); // no activate

        visitor.getFlag("rank", null).userExposed(); // activate
        visitor.getFlag("rank", "null").userExposed(); // no activate
        visitor.getFlag("rank_plus", "null").userExposed(); // activate
        visitor.getFlag("do_not_exists", "null").userExposed(); // no activate

        Thread.sleep(1500);
        assertEquals(6, activateLatch.getCount());

    }

}
