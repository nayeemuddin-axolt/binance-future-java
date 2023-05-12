package unit;

import java.util.LinkedHashMap;

public final class MockData {
    private static final int intValue2 = 2;
    public static final int HTTP_STATUS_OK = 200;
    public static final int HTTP_STATUS_CLIENT_ERROR = 400;
    public static final int HTTP_STATUS_SERVER_ERROR = 502;
    public static final String PREFIX = "/";
    public static final String MOCK_RESPONSE = "{\"key_1\": \"value_1\", \"key_2\": \"value_2\"}";
    public static final String API_KEY = "0a7dc879c2536ead5073d2a17c2c7510a1a4efbf8637db72eef5075ddc89fbd8";
    public static final String SECRET_KEY = "0805c1148517ad14fd44d60499fe763160d290ccadc1e358651380bff527a1c2";
    public static final LinkedHashMap<String, Object> MOCK_PARAMETERS = new LinkedHashMap<String, Object>() {{
        put("key1", "value1");
        put("key2", intValue2);
    }};

    private MockData() {
    }
}
