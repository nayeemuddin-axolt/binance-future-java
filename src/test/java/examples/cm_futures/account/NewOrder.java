package examples.cm_futures.account;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import examples.PrivateConfig;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
// import examples.cm_futures.market.MarkPrice;
// import org.json.JSONArray;
// import org.json.JSONObject;

public final class NewOrder {
    private NewOrder() {
    }

    private static final double quantity = 6;
    private static final double price = 2.5000;

    private static final String symbol = "ADAUSD_PERP";
    private static String type = "MARKET";

    private static final Logger logger = LoggerFactory.getLogger(NewOrder.class);

    private static final int zero = 0;
    private static final int eight = 8;
    //
    public static double round(double value, int places) {
        if (places < zero) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void main(String[] args) {

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY,
                PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);
        double roundedQuantity = round(quantity, eight);
        double roundedPrice = round(price, eight);

        // getting mark price
        // String markData = MarkPrice.main(new String[] { symbol });
        // JSONArray markArray = new JSONArray(markData);
        // JSONObject markObject = markArray.getJSONObject(0);
        // String markPrice = markObject.getString("markPrice");
        // double price = 0.0023; Double.parseDouble(markPrice);

        parameters.put("symbol", symbol);
        parameters.put("side", "BUY");
        parameters.put("type", type);
        parameters.put("quantity", roundedQuantity);

        if (type == "LIMIT") {
            parameters.put("timeInForce", "GTC");
            parameters.put("price", roundedPrice);
        }

        try {
            String result = client.account().newOrder(parameters);
            logger.info(result);
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }
    }
}