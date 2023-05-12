package examples.Indicator;

// import com.binance.connector.futures.client.exceptions.BinanceClientException;
// import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import examples.PrivateConfig;
// import examples.Indicator.TradingStrategies;
import java.util.LinkedHashMap;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import org.ta4j.core.Bar;
// import org.ta4j.core.BaseBar;
// import org.ta4j.core.BarSeriesManager;
// import org.ta4j.core.num.Num;
import org.ta4j.core.BaseBarSeries;
// import org.ta4j.core.BaseStrategy;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
// import org.ta4j.core.indicators.helpers.DifferenceIndicator;
// import org.ta4j.core.Rule;
// import org.ta4j.core.Strategy;
// import org.ta4j.core.trading.rules.OverIndicatorRule;
// import org.ta4j.core.trading.rules.UnderIndicatorRule;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.MACDIndicator;
// import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.indicators.ROCIndicator;
import org.ta4j.core.indicators.WilliamsRIndicator;
import com.google.common.util.concurrent.RateLimiter;

public class BinanceOrderFlowSystem implements OrderFlowSystem {

    private final RateLimiter rateLimiter;    

    public BinanceOrderFlowSystem() {
        this.rateLimiter = RateLimiter.create(10); // 10 requests per second
    }

    @Override
    public void placeBuyOrder(int timeFrame, String limitPrice) {
        try {
            rateLimiter.acquire(); // This will block until a permit is available

            CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY,
                PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);

            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", TradingStrategies.SYMBOL);
            parameters.put("side", "BUY");
            parameters.put("type", "LIMIT");
            parameters.put("quantity", "5");
            parameters.put("timeInForce", "GTC");
            parameters.put("price", limitPrice);
            String result = client.account().newOrder(parameters);
            System.out.println(result);
            // NewOrderResponse newOrderResponse = client.newOrder(NewOrder.limitBuy(SYMBOL, TimeInForce.GTC, "10", limitPrice));
            // System.out.println("New BUY order has been placed. " + " Order ID: " + newOrderResponse.getClientOrderId());
        } catch (Exception e) {
            System.err.println("Failed to place buy order: " + e.getMessage());
        }
    }

    @Override
    public void placeSellOrder(int timeFrame, String limitPrice) {
        try {
            rateLimiter.acquire(); // This will block until a permit is available

            CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY,
                PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);

            // Define your limit price here
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", TradingStrategies.SYMBOL);
            parameters.put("side", "SELL");
            parameters.put("type", "LIMIT");
            parameters.put("quantity", "5");
            parameters.put("timeInForce", "GTC");
            parameters.put("price", limitPrice);
            String result = client.account().newOrder(parameters);
            System.out.println(result);
            // NewOrderResponse newOrderResponse = client.newOrder(NewOrder.limitSell(SYMBOL, TimeInForce.GTC, "10", limitPrice));
            // System.out.println("New SELL order has been placed. " + " Order ID: " + newOrderResponse.getClientOrderId());
        } catch (Exception e) {
            System.err.println("Failed to place sell order: " + e.getMessage());
        }
    }

    @Override
    public void holdOrder(int timeFrame) {
        System.out.println("Hold order for time frame " + timeFrame);
    }

    public String getOrderFlowSignal(BaseBarSeries series, int timeFrame) {
        // Calculate the weighted average rate
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        double smaAvg = new SMAIndicator(closePrice, timeFrame).getValue(series.getEndIndex()).doubleValue();
        double emaAvg = new EMAIndicator(closePrice, timeFrame).getValue(series.getEndIndex()).doubleValue();
        double rsiAvg = new RSIIndicator(closePrice, timeFrame).getValue(series.getEndIndex()).doubleValue();
        double parabolicSarValue = new ParabolicSarIndicator(series).getValue(series.getEndIndex()).doubleValue();
        double macdValue = new MACDIndicator(closePrice).getValue(series.getEndIndex()).doubleValue();
        double cciValue = new CCIIndicator(series, timeFrame).getValue(series.getEndIndex()).doubleValue();
        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, timeFrame);
        double stochasticDValue = new StochasticOscillatorDIndicator(stochasticK).getValue(series.getEndIndex()).doubleValue();
        double obvValue = new OnBalanceVolumeIndicator(series).getValue(series.getEndIndex()).doubleValue();
        double rocValue = new ROCIndicator(closePrice, timeFrame).getValue(series.getEndIndex()).doubleValue();
        double williamsR = new WilliamsRIndicator(series, timeFrame).getValue(series.getEndIndex()).doubleValue();        

        // Calculate the weighted average
        double weightedAverage = smaAvg + emaAvg + rsiAvg + parabolicSarValue + macdValue + cciValue + stochasticDValue + obvValue + rocValue + williamsR;
        System.out.println("weightedAverage"+weightedAverage);
        // Define your threshold here
        double threshold = 111894;
        // Check the signal
        if (weightedAverage < threshold) {
            return "BUY";
        } else if (weightedAverage > threshold) {
            return "SELL";
        } else {
            return "HOLD";
        }
    }

    // public void getOrderStatus(String orderId) {
    //     try {
    //         rateLimiter.acquire(); // This will block until a permit is available
    //         OrderStatusRequest orderStatusRequest = new OrderStatusRequest(SYMBOL, Long.valueOf(orderId));
    //         System.out.println(client.getOrderStatus(orderStatusRequest));
    //     } catch (Exception e) {
    //         System.err.println("Failed to get order status: " + e.getMessage());
    //     }
    // }
}
