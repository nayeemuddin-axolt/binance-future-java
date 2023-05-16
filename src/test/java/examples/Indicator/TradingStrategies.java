package examples.Indicator;

// import com.binance.connector.futures.client.exceptions.BinanceClientException;
// import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import examples.Indicator.BinanceOrderFlowSystem;
// import org.json.JSONObject;

// import org.ta4j.core.BarSeriesManager;
// import org.ta4j.core.num.Num;
// import org.ta4j.core.BaseStrategy;
// import org.ta4j.core.indicators.helpers.DifferenceIndicator;
// import org.ta4j.core.Rule;
// import org.ta4j.core.Strategy;
// import org.ta4j.core.trading.rules.OverIndicatorRule;
// import org.ta4j.core.trading.rules.UnderIndicatorRule;
// import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
// import org.ta4j.core.TradingRecord;

import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import examples.PrivateConfig;
import java.util.LinkedHashMap;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.indicators.ROCIndicator;
import org.ta4j.core.indicators.WilliamsRIndicator;
import org.json.JSONArray;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Comparator;
import com.google.gson.Gson;

public class TradingStrategies {

    public static final String SYMBOL = "ADAUSD_PERP";
    public static final String PAIR = "ADAUSD";

    // private static final Logger logger =
    // LoggerFactory.getLogger(TradingStrategies.class);

    public static void main(String[] args) {

        BaseBarSeries series = new BaseBarSeries();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("pair", PAIR);
        parameters.put("contractType", "PERPETUAL");
        parameters.put("interval", "5m");

        CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY,
                PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);

        String continKlines = client.market().continuousKlines(parameters);
        JSONArray KlinesArray = new JSONArray(continKlines);

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
            ContinuousKlines obj = new ContinuousKlines(timestamp, open, high, low, close, volume, closeTimestamp,
                    quoteAssetVolume, numTrades, takerBuyBaseAssetVolume, takerBuyQuoteAssetVolume, ignore);
            myList.add(obj);
        }

        for (ContinuousKlines klines : myList) {
            ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(klines.getCloseTimestamp()),
                    ZoneId.systemDefault());
            BigDecimal openPrice = new BigDecimal(klines.getOpen());
            BigDecimal highPrice = new BigDecimal(klines.getHigh());
            BigDecimal lowPrice = new BigDecimal(klines.getLow());
            BigDecimal closePrice = new BigDecimal(klines.getClose());
            BigDecimal volume = new BigDecimal(klines.getVolume());
            series.addBar(
                    new BaseBar(Duration.ofHours(1), endTime, openPrice, highPrice, lowPrice, closePrice, volume));
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        int[] timeFrames = { 5, 10, 15, 30, 60, 120, 240 };

        double[] thresholds = { 0.01, 0.05, 0.1, 0.2, 0.5 };

        double riskPerTrade = 20.0;

        executeTradingStrategy(closePrice, timeFrames, series, thresholds, new BinanceOrderFlowSystem(), riskPerTrade,
                client);

        // AvgRate(closePrice, timeFrame, series);
    }

    public static void executeTradingStrategy(ClosePriceIndicator closePrice, int[] timeFrames, BaseBarSeries series,
            double[] thresholds, BinanceOrderFlowSystem orderFlowSystem, double riskPerTrade,
            CMFuturesClientImpl client) {

        // Define the weights for each indicator
        double[] weights = { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 }; // Adjust these weights based on your
                                                                                 // trading strategy

        ExecutorService executor = Executors.newFixedThreadPool(timeFrames.length);
        List<Future<Double>> futures = new ArrayList<>();

        // Calculate the weighted average rate for each time frame
        for (int i = 0; i < timeFrames.length; i++) {
            final int index = i;
            Future<Double> future = executor.submit(() -> {
                double smaAvg = weights[0]
                        * new SMAIndicator(closePrice, timeFrames[index]).getValue(series.getEndIndex()).doubleValue();
                double emaAvg = weights[1]
                        * new EMAIndicator(closePrice, timeFrames[index]).getValue(series.getEndIndex()).doubleValue();
                double rsiAvg = weights[2]
                        * new RSIIndicator(closePrice, timeFrames[index]).getValue(series.getEndIndex()).doubleValue();
                double parabolicSarValue = weights[3]
                        * new ParabolicSarIndicator(series).getValue(series.getEndIndex()).doubleValue();
                double macdValue = weights[4]
                        * new MACDIndicator(closePrice).getValue(series.getEndIndex()).doubleValue();
                double cciValue = weights[5]
                        * new CCIIndicator(series, timeFrames[index]).getValue(series.getEndIndex()).doubleValue();
                StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series,
                        timeFrames[index]);
                double stochasticDValue = weights[6]
                        * new StochasticOscillatorDIndicator(stochasticK).getValue(series.getEndIndex()).doubleValue();
                double obvValue = weights[7]
                        * new OnBalanceVolumeIndicator(series).getValue(series.getEndIndex()).doubleValue();
                double rocValue = weights[8]
                        * new ROCIndicator(closePrice, timeFrames[index]).getValue(series.getEndIndex()).doubleValue();
                double williamsR = weights[9] * new WilliamsRIndicator(series, timeFrames[index])
                        .getValue(series.getEndIndex()).doubleValue();

                // Calculate the weighted average rate
                return smaAvg + emaAvg + rsiAvg + parabolicSarValue + macdValue + cciValue + stochasticDValue + obvValue
                        + rocValue + williamsR;
            });
            futures.add(future);
        }

        double lowestPrice = series.getBarData().stream().limit(100).map(Bar::getLowPrice)
                .min(Comparator.naturalOrder()).orElseThrow().doubleValue();
        double highestPrice = series.getBarData().stream().limit(100).map(Bar::getHighPrice)
                .max(Comparator.naturalOrder()).orElseThrow().doubleValue();

        double profitTarget = lowestPrice * 1.05;
        double stopLoss = lowestPrice * 0.97;

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        // Fetch the portfolio size
        String fetchBalance = client.account().futuresAccountBalance(parameters);

        Gson gson = new Gson();
        MyObject[] myObjects = gson.fromJson(fetchBalance, MyObject[].class);

        String[] parts = SYMBOL.split("(?=USD)");
        String targetAsset = parts[0];
        double portfolioSize = 0.0;
        System.out.println(targetAsset);
        for (MyObject myObject : myObjects) {
            if (myObject.asset.equals(targetAsset)) {
                portfolioSize = myObject.balance;
                break;
            }
        }

        // Calculate order size based on risk per trade
        double orderSize = portfolioSize * riskPerTrade;

        System.out.println("orderSize " + orderSize);
        System.out.println("lowestPrice " + lowestPrice);
        System.out.println("highestPrice " + highestPrice);
        System.out.println("profitTarget " + profitTarget);
        System.out.println("stopLoss " + stopLoss);

        // for (int i = 0; i < futures.size(); i++) {
        try {
            // Evaluate the order flow before making a decision
            String orderFlowSignal = orderFlowSystem.getOrderFlowSignal(series, timeFrames[0]);
            // double weightedAvgRate = futures.get(i).get();
            // System.out.println("weightedAvgRate"+weightedAvgRate);
            // double finalScore = weightedAvgRate + orderFlowScore;
            // System.out.println("orderFlowSignal"+orderFlowSignal);
            if (orderFlowSignal.equals("BUY")) {
                orderFlowSystem.placeBuyOrder(timeFrames[0], Double.toString(lowestPrice));
            } else if (orderFlowSignal.equals("SELL")) {
                orderFlowSystem.placeSellOrder(timeFrames[0], Double.toString(highestPrice));
            } else {
                orderFlowSystem.holdOrder(timeFrames[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // }
    }

    public class MyObject {
        private String asset;
        private double balance;
    }
    /*
     * public static void AvgRate(ClosePriceIndicator closePrice, int timeFrame,
     * BaseBarSeries series) {
     * //threshold is a threshold value that you can adjust to determine when to buy
     * or sell
     * double threshold = 50000;
     * 
     * // Calculate the average value of each indicator over the specified time
     * frame
     * double smaAvg = new SMAIndicator(closePrice,
     * timeFrame).getValue(series.getEndIndex()).doubleValue();
     * double emaAvg = new EMAIndicator(closePrice,
     * timeFrame).getValue(series.getEndIndex()).doubleValue();
     * double rsiAvg = new RSIIndicator(closePrice,
     * timeFrame).getValue(series.getEndIndex()).doubleValue();
     * double parabolicSarValue = new
     * ParabolicSarIndicator(series).getValue(series.getEndIndex()).doubleValue();
     * double macdValue = new
     * MACDIndicator(closePrice).getValue(series.getEndIndex()).doubleValue();
     * double cciValue = new CCIIndicator(series,
     * timeFrame).getValue(series.getEndIndex()).doubleValue();
     * StochasticOscillatorKIndicator stochasticK = new
     * StochasticOscillatorKIndicator(series, timeFrame);
     * double stochasticDValue = new
     * StochasticOscillatorDIndicator(stochasticK).getValue(series.getEndIndex()).
     * doubleValue();
     * double obvValue = new
     * OnBalanceVolumeIndicator(series).getValue(series.getEndIndex()).doubleValue()
     * ;
     * double rocValue = new ROCIndicator(closePrice,
     * timeFrame).getValue(series.getEndIndex()).doubleValue();
     * double williamsR = new WilliamsRIndicator(series,
     * timeFrame).getValue(series.getEndIndex()).doubleValue();
     * 
     * // Calculate the average rate
     * double avgRate = (smaAvg + emaAvg + rsiAvg + parabolicSarValue + macdValue +
     * cciValue + stochasticDValue + obvValue + rocValue + williamsR) / 10;
     * 
     * System.out.println("avgRate"+avgRate);
     * 
     * // Determine whether to buy, sell, or hold based on the average rate
     * if (avgRate > threshold) {
     * System.out.println("to place buy order");
     * // Place a buy order
     * } else if (avgRate < threshold) {
     * System.out.println("to place sell order");
     * // Place a sell order
     * } else {
     * System.out.println("Order is on Hold");
     * // Hold the security
     * }
     * }
     * 
     * public static double[] SMACalculation(ClosePriceIndicator closePrice, int
     * timeFrame, BaseBarSeries series) {
     * SMAIndicator sma = new SMAIndicator(closePrice, timeFrame);
     * // Calculate the SMA value for the most recent bar in the series
     * int lastIndex = series.getEndIndex();
     * double smaValue = sma.getValue(lastIndex).doubleValue();
     * 
     * // Get the close price of the most recent bar using the ClosePriceIndicator
     * double clsPrice = closePrice.getValue(lastIndex).doubleValue();
     * double[] results = { clsPrice, smaValue };
     * return results;
     * }
     * 
     * public static double[] EMACalculation(ClosePriceIndicator closePrice, int
     * timeFrame, BaseBarSeries series) {
     * EMAIndicator ema = new EMAIndicator(closePrice, timeFrame);
     * // Calculate the EMA value for the most recent bar in the series
     * int lastIndex = series.getEndIndex();
     * double emaValue = ema.getValue(lastIndex).doubleValue();
     * 
     * // Get the close price of the most recent bar using the ClosePriceIndicator
     * double clsPrice = closePrice.getValue(lastIndex).doubleValue();
     * double[] results = { clsPrice, emaValue };
     * return results;
     * }
     * 
     * public static double[] RSICalculation(ClosePriceIndicator closePrice, int
     * timeFrame, BaseBarSeries series) {
     * RSIIndicator rsi = new RSIIndicator(closePrice, timeFrame);
     * // Calculate the EMA value for the most recent bar in the series
     * int lastIndex = series.getEndIndex();
     * double rsiValue = rsi.getValue(lastIndex).doubleValue();
     * 
     * // Get the close price of the most recent bar using the ClosePriceIndicator
     * double clsPrice = closePrice.getValue(lastIndex).doubleValue();
     * double[] results = { clsPrice, rsiValue };
     * return results;
     * }
     * 
     * public static void MACDCalculation(ClosePriceIndicator closePrice, int
     * timeFrame, BaseBarSeries series) {
     * MACDIndicator macd = new MACDIndicator(closePrice);
     * 
     * // Get the MACD line values
     * double[] macdValues = new double[series.getBarCount()];
     * for (int i = 0; i < macdValues.length; i++) {
     * macdValues[i] = macd.getValue(i).doubleValue();
     * }
     * 
     * // Calculate the signal line values using an EMAIndicator
     * EMAIndicator emaSignal = new EMAIndicator(macd, timeFrame);
     * double[] signalValues = new double[series.getBarCount()];
     * for (int i = 0; i < signalValues.length; i++) {
     * signalValues[i] = emaSignal.getValue(i).doubleValue();
     * }
     * 
     * // Calculate the histogram values using a DifferenceIndicator
     * DifferenceIndicator histogram = new DifferenceIndicator(macd, emaSignal);
     * double[] histogramValues = new double[series.getBarCount()];
     * for (int i = 0; i < histogramValues.length; i++) {
     * histogramValues[i] = histogram.getValue(i).doubleValue();
     * }
     * 
     * // Print the MACD, signal, and histogram values
     * for (int i = 0; i < series.getBarCount(); i++) {
     * System.out.println(
     * "MACD: " + macdValues[i] + " Signal: " + signalValues[i] + " Histogram: " +
     * histogramValues[i]);
     * }
     * 
     * boolean inPosition = false;
     * double lastBuyPrice = 0;
     * 
     * // Loop through the bars in the TimeSeries
     * for (int i = 1; i < series.getBarCount(); i++) {
     * // Check for a buy signal (MACD crosses above signal line)
     * if (macdValues[i] > signalValues[i] && macdValues[i - 1] <= signalValues[i -
     * 1]) {
     * // If not already in a position, buy at the current price
     * if (!inPosition) {
     * inPosition = true;
     * lastBuyPrice = series.getBar(i).getClosePrice().doubleValue();
     * System.out.println("Buy at " + lastBuyPrice);
     * }
     * }
     * // Check for a sell signal (MACD crosses below signal line)
     * else if (macdValues[i] < signalValues[i] && macdValues[i - 1] >=
     * signalValues[i - 1]) {
     * // If in a long position, sell at the current price
     * if (inPosition) {
     * inPosition = false;
     * double sellPrice = series.getBar(i).getClosePrice().doubleValue();
     * double profit = sellPrice - lastBuyPrice;
     * System.out.println("Sell at " + sellPrice + ", profit = " + profit);
     * }
     * }
     * }
     * }
     * 
     * public static void PBCalculation(ClosePriceIndicator closePrice, int
     * timeFrame, BaseBarSeries series) {
     * ParabolicSarIndicator parabolicSar = new ParabolicSarIndicator(series);
     * Rule buySignal = new OverIndicatorRule(parabolicSar,
     * series.getBar(series.getEndIndex()).getClosePrice());
     * Rule sellSignal = new UnderIndicatorRule(parabolicSar,
     * series.getBar(series.getEndIndex()).getClosePrice());
     * 
     * // Use the rules to create a trading strategy
     * Strategy strategy = new BaseStrategy(buySignal, sellSignal);
     * 
     * BarSeriesManager seriesManager = new BarSeriesManager(series);
     * TradingRecord tradingRecord = seriesManager.run(strategy);
     * Num profit = new TotalProfitCriterion().calculate(series, tradingRecord);
     * 
     * System.out.println(profit);
     * 
     * }
     */
}
