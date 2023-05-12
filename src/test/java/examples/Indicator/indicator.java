package examples.Indicator;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import examples.PrivateConfig;
// import examples.Indicator.ContinuousKlines;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class indicator {
    private indicator() {
    }

    private static final String symbol = "ADAUSD_230929";
    private static final String pair = "ADAUSD";
    private static final double quantity = 5;

    private static final Logger logger = LoggerFactory.getLogger(indicator.class);

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void main(String[] args) {

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY,
                PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);

        parameters.put("symbol", symbol);
        try {
            String tickerSymbol = client.market().tickerSymbol(parameters);
            JSONArray markArray = new JSONArray(tickerSymbol);
            JSONObject markObject = markArray.getJSONObject(0);
            Double markPrice = Double.parseDouble(markObject.getString("price"));

            double sma50 = calculateSMA(client, symbol, 500, parameters, pair);

            double roundedQuantity = round(quantity, 8);

            parameters.put("type", "MARKET");
            parameters.put("quantity", roundedQuantity);

            System.out.println(markPrice);
            System.out.println(sma50);

            if (markPrice > sma50) {
                parameters.put("side", "BUY");
                String BuyOrder = client.account().newOrder(parameters);
                logger.info("BuyOrder" + BuyOrder);
            } else {
                parameters.put("side", "SELL");
                String SellOrder = client.account().newOrder(parameters);
                logger.info("SellOrder" + SellOrder);
            }

        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }

    }

    private static double calculateSMA(CMFuturesClientImpl client, String symbol, int period,
            LinkedHashMap<String, Object> parameters, String pair) {
        parameters.put("pair", pair);
        parameters.put("contractType", "PERPETUAL");
        parameters.put("interval", "5m");
        String continKlines = client.market().continuousKlines(parameters);
        JSONArray KlinesArray = new JSONArray(continKlines);
        double sum = 0;
        List<ContinuousKlines> myList = new ArrayList<>();

        for (int i = 0; i < KlinesArray.length(); i++) {
            JSONArray row = KlinesArray.getJSONArray(i);
            long timestamp = row.getLong(0);
            double open = row.getDouble(1);
            double high = row.getDouble(2);
            double low = row.getDouble(3);
            double close = row.getDouble(4);
            double volume = row.getDouble(5);
            long closeTimestamp = row.getLong(6);
            double quoteAssetVolume = row.getDouble(7);
            double numTrades = row.getDouble(8);
            double takerBuyBaseAssetVolume = row.getDouble(9);
            double takerBuyQuoteAssetVolume = row.getDouble(10);
            double ignore = row.getDouble(11);
            ContinuousKlines obj = new ContinuousKlines(timestamp,open,high,low,close,volume,closeTimestamp,quoteAssetVolume,numTrades,takerBuyBaseAssetVolume,takerBuyQuoteAssetVolume,ignore);
            sum += obj.getClose();
            myList.add(obj);
        }

        return sum / period;
    }
}