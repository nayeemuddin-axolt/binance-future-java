package examples.Indicator;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradingStrategies {

        public static final String SYMBOL = "ADAUSD_PERP";

        
        private static final Logger logger = LoggerFactory.getLogger(TradingStrategies.class);

        public static void main(String[] args) {

                BaseBarSeries series = new BaseBarSeries();

                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                String[] parts = SYMBOL.split("(?=_)");
                String PAIR = parts[0];
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
                        ContinuousKlines obj = new ContinuousKlines(timestamp, open, high, low, close, volume,
                                        closeTimestamp,
                                        quoteAssetVolume, numTrades, takerBuyBaseAssetVolume, takerBuyQuoteAssetVolume,
                                        ignore);
                        myList.add(obj);
                }

                for (ContinuousKlines klines : myList) {
                        ZonedDateTime endTime = ZonedDateTime.ofInstant(
                                        Instant.ofEpochMilli(klines.getCloseTimestamp()),
                                        ZoneId.systemDefault());
                        BigDecimal openPrice = new BigDecimal(klines.getOpen());
                        BigDecimal highPrice = new BigDecimal(klines.getHigh());
                        BigDecimal lowPrice = new BigDecimal(klines.getLow());
                        BigDecimal closePrice = new BigDecimal(klines.getClose());
                        BigDecimal volume = new BigDecimal(klines.getVolume());
                        series.addBar(
                                        new BaseBar(Duration.ofHours(1), endTime, openPrice, highPrice, lowPrice,
                                                        closePrice, volume));
                }

                ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

                int[] timeFrames = { 5, 10, 15, 30, 60, 120, 240 };

                double riskPerTrade = 5.0;

                executeTradingStrategy(closePrice, timeFrames, series, new BinanceOrderFlowSystem(),
                                riskPerTrade,
                                client);
        }

        public static void executeTradingStrategy(ClosePriceIndicator closePrice, int[] timeFrames,
                        BaseBarSeries series, BinanceOrderFlowSystem orderFlowSystem, double riskPerTrade,
                        CMFuturesClientImpl client) {

                // Define the weights for each indicator
                double[] weights = { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 }; // Adjust these weights based
                                                                                         // on your
                                                                                         // trading strategy

                ExecutorService executor = Executors.newFixedThreadPool(timeFrames.length);
                List<Future<Double>> futures = new ArrayList<>();

                // Calculate the weighted average rate for each time frame
                List<TimeAvg> TimeAvList = new ArrayList<>();
                for (int i = 0; i < timeFrames.length; i++) {
                        final int index = i;
                        int Timelen = timeFrames.length;
                        Future<Double> future = executor.submit(() -> {
                                double avg = 0.0;
                                if (index >= 0 && index < Timelen) {
                                        double smaAvg = weights[0]
                                                        * new SMAIndicator(closePrice, timeFrames[index])
                                                                        .getValue(series.getEndIndex()).doubleValue();
                                        double emaAvg = weights[1]
                                                        * new EMAIndicator(closePrice, timeFrames[index])
                                                                        .getValue(series.getEndIndex()).doubleValue();
                                        double rsiAvg = weights[2]
                                                        * new RSIIndicator(closePrice, timeFrames[index])
                                                                        .getValue(series.getEndIndex()).doubleValue();
                                        double parabolicSarValue = weights[3]
                                                        * new ParabolicSarIndicator(series)
                                                                        .getValue(series.getEndIndex())
                                                                        .doubleValue();
                                        double macdValue = weights[4]
                                                        * new MACDIndicator(closePrice).getValue(series.getEndIndex())
                                                                        .doubleValue();
                                        double cciValue = weights[5]
                                                        * new CCIIndicator(series, timeFrames[index])
                                                                        .getValue(series.getEndIndex()).doubleValue();
                                        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(
                                                        series,
                                                        timeFrames[index]);
                                        double stochasticDValue = weights[6]
                                                        * new StochasticOscillatorDIndicator(stochasticK)
                                                                        .getValue(series.getEndIndex()).doubleValue();
                                        double obvValue = weights[7]
                                                        * new OnBalanceVolumeIndicator(series)
                                                                        .getValue(series.getEndIndex())
                                                                        .doubleValue();
                                        double rocValue = weights[8]
                                                        * new ROCIndicator(closePrice, timeFrames[index])
                                                                        .getValue(series.getEndIndex()).doubleValue();
                                        double williamsR = weights[9]
                                                        * new WilliamsRIndicator(series, timeFrames[index])
                                                                        .getValue(series.getEndIndex()).doubleValue();

                                        // Calculate the weighted average rate

                                        int count = 0;

                                        if (!Double.isNaN(smaAvg)) {
                                                avg += smaAvg;
                                                count++;
                                        }

                                        if (!Double.isNaN(emaAvg)) {
                                                avg += emaAvg;
                                                count++;
                                        }

                                        if (!Double.isNaN(rsiAvg)) {
                                                avg += rsiAvg;
                                                count++;
                                        }

                                        if (!Double.isNaN(parabolicSarValue)) {
                                                avg += parabolicSarValue;
                                                count++;
                                        }

                                        if (!Double.isNaN(macdValue)) {
                                                avg += macdValue;
                                                count++;
                                        }

                                        if (!Double.isNaN(cciValue)) {
                                                avg += cciValue;
                                                count++;
                                        }

                                        if (!Double.isNaN(stochasticDValue)) {
                                                avg += stochasticDValue;
                                                count++;
                                        }

                                        if (!Double.isNaN(obvValue)) {
                                                avg += obvValue;
                                                count++;
                                        }

                                        if (!Double.isNaN(rocValue)) {
                                                avg += rocValue;
                                                count++;
                                        }

                                        if (!Double.isNaN(williamsR)) {
                                                avg += williamsR;
                                                count++;
                                        }

                                        if (count > 0) {
                                                avg /= count;
                                        } else {
                                                // Handle the case when all values are NaN
                                        }

                                        if (!Double.isNaN(avg)) {
                                                TradingStrategies outerObj = new TradingStrategies();
                                                TimeAvg TimeObj = outerObj.new TimeAvg();

                                                // Extract the id and name properties from the original object and
                                                // assign them to the formatted object
                                                TimeObj.timeframe = timeFrames[index];
                                                TimeObj.Average = avg;
                                                TimeObj.smaAvg = smaAvg;
                                                TimeObj.emaAvg = emaAvg;
                                                TimeObj.rsiAvg = rsiAvg;
                                                TimeObj.macdValue = macdValue;
                                                TimeObj.cciValue = cciValue;
                                                TimeObj.williamsR = williamsR;
                                                TimeObj.stochasticDValue = stochasticDValue;
                                                TimeObj.parabolicSarValue = parabolicSarValue;
                                                TimeObj.obvValue = obvValue;
                                                TimeObj.rocValue = rocValue;
                                                // Add the formatted object to the list
                                                TimeAvList.add(TimeObj);
                                        }
                                }
                                return avg;
                        });
                        futures.add(future);
                }

                // Fetch the averages
                List<Double> averages = new ArrayList<>();
                for (Future<Double> future : futures) {
                        try {
                                averages.add(future.get());
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                }

                Gson gson = new Gson();
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

                // Fetch the portfolio size
                String fetchBalance = client.account().futuresAccountBalance(parameters);

                MyObject[] myObjects = gson.fromJson(fetchBalance, MyObject[].class);

                String[] parts = SYMBOL.split("(?=USD)");
                String targetAsset = parts[0];
                double portfolioSize = 0.0;
                for (MyObject myObject : myObjects) {
                        if (myObject.asset.equals(targetAsset)) {
                                portfolioSize = myObject.balance;
                                break;
                        }
                }

                double lowestPrice = series.getBarData().stream().limit(100).map(Bar::getLowPrice)
                                .min(Comparator.naturalOrder()).orElseThrow().doubleValue();
                double highestPrice = series.getBarData().stream().limit(100).map(Bar::getHighPrice)
                                .max(Comparator.naturalOrder()).orElseThrow().doubleValue();

                // Define the thresholds
                double buyThreshold = -205.5;
                double sellThreshold = -205.5;

                logger.info("\nAsset Balance of " + targetAsset + " is " + portfolioSize +
                                "\nLowestPrice to buy at " + lowestPrice +
                                "\nHighestPrice to sell at " + highestPrice +
                                "\nDefined Buy Threshold: " + buyThreshold +
                                "\nDefined Sell Threshold: " + sellThreshold + "\n");
                // --- order book ---
                LinkedHashMap<String, Object> OrdParam = new LinkedHashMap<>();

                OrdParam.put("symbol", SYMBOL);
                OrdParam.put("limit", "10"); // Valid limits:[5, 10, 20, 50, 100, 500, 1000]

                String orderBookStr = client.market().depth(OrdParam);

                String OrderStr2 = convertOrderStr(orderBookStr);

                OrderBook orderBook = gson.fromJson(OrderStr2, OrderBook.class);

                // --- order book ---

                String orderFlowSignal = orderFlowSystem.getOrderFlowSignal(orderBook);
                logger.info("Order Flow Signal: " + orderFlowSignal);
                for (double average : averages) {
                        for (TimeAvg TAvg : TimeAvList) {
                                if (TAvg.Average == average) {
                                        logger.info("\nTime Frame at " + TAvg.timeframe
                                                        + "\nSimple Moving Average (SMA): " + TAvg.smaAvg
                                                        + "\nExponential Moving Average (EMA): " + TAvg.emaAvg
                                                        + "\nRelative Strength Index (RSI): " + TAvg.rsiAvg
                                                        + "\nMoving Average Convergence Divergence (MACD): "
                                                        + TAvg.macdValue + "\nCommodity Channel Index (CCI): "
                                                        + TAvg.cciValue + "\nRate of Change (ROC): " + TAvg.rocValue
                                                        + "\nWilliams %R: " + TAvg.williamsR + "\nStochastic %D: "
                                                        + TAvg.stochasticDValue
                                                        + "\nParabolic SAR (Stop and Reverse): "
                                                        + TAvg.parabolicSarValue + "\nOn-Balance Volume (OBV): "
                                                        + TAvg.obvValue + "\nTotal Average Indicator: " + TAvg.Average
                                                        + "\n--------------------------------------------------------\n");
                                        try {
                                                // Evaluate the order flow before making a decision
                                                if (orderFlowSignal.equals("BUY") && average > buyThreshold) {
                                                        if (portfolioSize > 0) {
                                                                orderFlowSystem.placeBuyOrder(
                                                                                Double.toString(lowestPrice));
                                                        } else {
                                                                logger.info("Asset Balance is 0");
                                                        }
                                                } else if (orderFlowSignal.equals("SELL") && average < sellThreshold) {
                                                        orderFlowSystem.placeSellOrder(Double.toString(highestPrice));
                                                } else {
                                                        orderFlowSystem.holdOrder();
                                                }
                                        } catch (Exception e) {
                                                e.printStackTrace();
                                        }
                                }
                        }
                }
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

        public class MyObject {
                private String asset;
                private double balance;
        }

        class TimeAvg {
                private int timeframe;
                private double Average;
                private double smaAvg;
                private double emaAvg;
                private double rsiAvg;
                private double macdValue;
                private double cciValue;
                private double williamsR;
                private double stochasticDValue;
                private double parabolicSarValue;
                private double obvValue;
                private double rocValue;
        }
}