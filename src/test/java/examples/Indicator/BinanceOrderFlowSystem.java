package examples.Indicator;

import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import examples.PrivateConfig;
import java.util.LinkedHashMap;
import com.google.common.util.concurrent.RateLimiter;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinanceOrderFlowSystem {

    private static final Logger logger = LoggerFactory.getLogger(BinanceOrderFlowSystem.class);

    private final RateLimiter rateLimiter;

    public BinanceOrderFlowSystem() {
        this.rateLimiter = RateLimiter.create(10); // 10 requests per second
    }

    
    public void placeBuyOrder(String limitPrice) {
        try {
            rateLimiter.acquire(); // This will block until a permit is available

            CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY,
                    PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);

            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", TradingStrategies.SYMBOL);
            parameters.put("side", "BUY");
            parameters.put("type", "LIMIT");
            parameters.put("quantity", "1");
            parameters.put("timeInForce", "GTC");
            parameters.put("price", limitPrice);
            String result = client.account().newOrder(parameters);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(result);
            long orderId = jsonNode.get("orderId").asLong();
            String symbol = jsonNode.get("symbol").asText();
            String type = jsonNode.get("type").asText();
            String clientOrderId = jsonNode.get("clientOrderId").asText();
            String side = jsonNode.get("side").asText();
            logger.info("\nOrder ID: " + orderId+
            "\nSymbol: " + symbol+
            "\nType: " + type+
            "\nClient Order ID: " + clientOrderId+
            "\nSide: " + side+
            "\nPrice: "+ limitPrice+
            "\n--------------------------------------------------------\n");
        } catch (Exception e) {
            System.err.println("Failed to place buy order: " + e.getMessage());
        }
    }

    
    public void placeSellOrder(String limitPrice) {
        try {
            rateLimiter.acquire(); // This will block until a permit is available

            CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY,
                    PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);

            // Define your limit price here
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", TradingStrategies.SYMBOL);
            parameters.put("side", "SELL");
            parameters.put("type", "LIMIT");
            parameters.put("quantity", "1");
            parameters.put("timeInForce", "GTC");
            parameters.put("price", limitPrice);
            String result = client.account().newOrder(parameters);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(result);
            long orderId = jsonNode.get("orderId").asLong();
            String symbol = jsonNode.get("symbol").asText();
            String type = jsonNode.get("type").asText();
            String clientOrderId = jsonNode.get("clientOrderId").asText();
            String side = jsonNode.get("side").asText();
            logger.info("\nOrder ID: " + orderId+
            "\nSymbol: " + symbol+
            "\nType: " + type+
            "\nClient Order ID: " + clientOrderId+
            "\nSide: " + side+
            "\nPrice: "+ limitPrice+
            "\n--------------------------------------------------------\n");
        } catch (Exception e) {
            System.err.println("Failed to place sell order: " + e.getMessage());
        }
    }

    
    public void holdOrder() {
        logger.info("Order Status Hold"+
        "\n--------------------------------------------------------\n");
    }

    public String getOrderFlowSignal(OrderBook orderBook) {
        List<OrderBookEntry> bids = orderBook.getBids();
        List<OrderBookEntry> asks = orderBook.getAsks();

        // Calculate the total bid quantity and ask quantity
        double totalBidQty = bids.stream().mapToDouble(entry -> Double.parseDouble(entry.getQty())).sum();
        double totalAskQty = asks.stream().mapToDouble(entry -> Double.parseDouble(entry.getQty())).sum();

        // Imbalance
        double imbalance = (totalBidQty - totalAskQty) / (totalBidQty + totalAskQty);
        
        // Spread Analysis
        double highestBid = Double.parseDouble(bids.get(0).getPrice());
        double lowestAsk = Double.parseDouble(asks.get(0).getPrice());
        double spread = lowestAsk - highestBid;
        logger.info("\nOrder Book Imbalance: " + imbalance+
        "\nSpread: " + spread);

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
