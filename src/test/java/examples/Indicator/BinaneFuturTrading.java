// package examples.Indicator;

// import com.binance.client.RequestOptions;
// import com.binance.client.SyncRequestClient;
// import com.binance.client.examples.constants.PrivateConfig;
// import com.binance.client.model.enums.*;
// import com.binance.client.model.market.PriceChangeTicker;
// import com.binance.client.model.market.SymbolPrice;
// import com.binance.client.model.trade.Order;
// import com.binance.client.model.trade.OrderSide;
// import com.binance.client.model.trade.OrderType;

// import java.math.BigDecimal;
// import java.util.List;

// public class BinanceFuturesTrading {
//     private static final RequestOptions REQUEST_OPTIONS = new RequestOptions();

//     static {
//         REQUEST_OPTIONS.setUrl("https://fapi.binance.com");
//         REQUEST_OPTIONS.setApiKey(PrivateConfig.API_KEY);
//         REQUEST_OPTIONS.setSecretKey(PrivateConfig.SECRET_KEY);
//     }

//     public static void main(String[] args) {
//         SyncRequestClient client = SyncRequestClient.create(REQUEST_OPTIONS);

//         // Get the latest market price
//         SymbolPrice price = client.getSymbolPriceTicker("BTCUSDT");

//         // Calculate the 50-period simple moving average
//         double sma50 = calculateSMA(client, "BTCUSDT", 50);

//         // Determine whether to buy or sell based on the current price and SMA50
//         if (new BigDecimal(price.getPrice()).doubleValue() > sma50) {
//             // Execute a buy order
//             Order order = client.postOrder("BTCUSDT", OrderSide.BUY, OrderType.MARKET, null, "1000", null, null, null, null, null, null, null);
//         } else {
//             // Execute a sell order
//             Order order = client.postOrder("BTCUSDT", OrderSide.SELL, OrderType.MARKET, null, "1000", null, null, null, null, null, null, null);
//         }
//     }

//     private static double calculateSMA(SyncRequestClient client, String symbol, int period) {
//         List<PriceChangeTicker> klines = client.getContinuousContractKlines(symbol, ContractType.PERPETUAL, null, null, null, null, null, null, period, null);

//         double sum = 0;
//         for (PriceChangeTicker kline : klines) {
//             sum += Double.parseDouble(kline.getClose());
//         }

//         return sum / period;
//     }
// }

