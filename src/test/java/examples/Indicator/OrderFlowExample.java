package examples.Indicator;

import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import examples.PrivateConfig;
import java.util.LinkedHashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class OrderFlowExample {

    private static final String symbol = "ADAUSD_PERP";

    public static void main(String[] args) {
        // Create a new Binance API client
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY,
                PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);

        parameters.put("symbol", symbol);
        parameters.put("limit", "10");

        String orderBookStr = client.market().depth(parameters);
        Gson gson = new Gson();

        String OrderStr2 = convertOrderStr(orderBookStr);

        OrderBook orderBook = gson.fromJson(OrderStr2, OrderBook.class);

        String OBret = analyzeOrderBook(orderBook);
    }

    private static String convertOrderStr(String OrderStr1) {
        Gson gson = new Gson();
        JsonObject orderObj1 = gson.fromJson(OrderStr1, JsonObject.class);

        JsonObject orderObj2 = new JsonObject();
        orderObj2.addProperty("lastUpdateId", orderObj1.get("lastUpdateId").getAsLong());

        JsonArray bidsArray1 = orderObj1.getAsJsonArray("bids");
        JsonArray bidsArray2 = new JsonArray();
        for (JsonElement bidElement : bidsArray1) {
            JsonArray bidArray = bidElement.getAsJsonArray();
            JsonObject bidObj = new JsonObject();
            bidObj.addProperty("price", bidArray.get(0).getAsString());
            bidObj.addProperty("qty", bidArray.get(1).getAsString());
            bidsArray2.add(bidObj);
        }
        orderObj2.add("bids", bidsArray2);

        JsonArray asksArray1 = orderObj1.getAsJsonArray("asks");
        JsonArray asksArray2 = new JsonArray();
        for (JsonElement askElement : asksArray1) {
            JsonArray askArray = askElement.getAsJsonArray();
            JsonObject askObj = new JsonObject();
            askObj.addProperty("price", askArray.get(0).getAsString());
            askObj.addProperty("qty", askArray.get(1).getAsString());
            asksArray2.add(askObj);
        }
        orderObj2.add("asks", asksArray2);

        return gson.toJson(orderObj2);
    }

    private static void printOrderBookEntries(List<OrderBookEntry> entries) {
        for (OrderBookEntry entry : entries) {
            System.out.println("Price: " + entry.getPrice() + ", Qty: " + entry.getQty());
        }
    }

    public static String analyzeOrderBook(OrderBook orderBook) {
        List<OrderBookEntry> bids = orderBook.getBids();
        List<OrderBookEntry> asks = orderBook.getAsks();

        // Calculate the total bid quantity and ask quantity
        double totalBidQty = bids.stream().mapToDouble(entry -> Double.parseDouble(entry.getQty())).sum();
        double totalAskQty = asks.stream().mapToDouble(entry -> Double.parseDouble(entry.getQty())).sum();

        // Imbalance
        double imbalance = (totalBidQty - totalAskQty) / (totalBidQty + totalAskQty);
        System.out.println("Order Book Imbalance: " + imbalance);

        // Spread Analysis
        double highestBid = Double.parseDouble(bids.get(0).getPrice());
        double lowestAsk = Double.parseDouble(asks.get(0).getPrice());
        double spread = lowestAsk - highestBid;
        System.out.println("Spread: " + spread);

        // Your trading strategy goes here.
        // In this example, if imbalance is positive (more buying pressure) and spread is small, we buy.
        // If imbalance is negative (more selling pressure) and spread is small, we sell.
        // If spread is large, we do nothing (hold).
        // These rules are completely arbitrary and are just for demonstration purposes.
        if (imbalance > 0 && spread < 0.01) {
            return "BUY";
        } else if (imbalance < 0 && spread < 0.01) {
            return "SELL";
        }else {
            return "HOLD";
        }
    }
}
