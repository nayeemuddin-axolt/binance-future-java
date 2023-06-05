package examples.Indicator;

import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import examples.PrivateConfig;

import java.util.LinkedHashMap;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.ROCIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.indicators.WilliamsRIndicator;
import org.ta4j.core.Strategy;
import org.ta4j.core.Rule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;
import org.ta4j.core.TradingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.Instant;
import java.time.Clock;
import java.time.ZoneId;
import org.ta4j.core.analysis.criteria.*;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;
import java.time.LocalDateTime;

public class backtesting {

    public static final String SYMBOL = "BTCUSD_PERP";

    private static final Logger logger = LoggerFactory.getLogger(TradingStrategies.class);

    public static void main(String[] args) {

        String begin = "2022-12-01 00:00:00"; // yyyy-MM-dd HH:mm:ss
        String end = "now";

        LocalDateTime dateTime = LocalDateTime.parse(begin.replace(" ", "T"));
        long beginMillis = dateTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        long endInMillis;

        if (end.equalsIgnoreCase("now")) {
            Instant currentInstant = Instant.now(Clock.systemUTC());
            endInMillis = currentInstant.toEpochMilli();
        } else {
            LocalDateTime dateTime1 = LocalDateTime.parse(end.replace(" ", "T"));
            endInMillis = dateTime1.toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        }

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String[] parts = SYMBOL.split("(?=_)");
        String PAIR = parts[0];
        parameters.put("pair", PAIR);
        parameters.put("contractType", "PERPETUAL");
        parameters.put("interval", "5m");
        parameters.put("startTime", beginMillis);
        parameters.put("endTime", endInMillis);

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

        List<Bar> bars = new ArrayList<>();
        for (ContinuousKlines klines : myList) {
            ZonedDateTime endTime = Instant.ofEpochMilli(klines.getCloseTimestamp()).atZone(ZoneId.systemDefault());
            double open = klines.getOpen();
            double high = klines.getHigh();
            double low = klines.getLow();
            double close = klines.getClose();
            double volume = klines.getVolume();
            bars.add(new BaseBar(Duration.ofHours(1), endTime, open, high, low, close, volume));
        }

        BaseBarSeries series = new BaseBarSeries(bars);
        int shortSmaPeriod = 5;
        int longSmaPeriod = 15;
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, shortSmaPeriod);
        SMAIndicator longSma = new SMAIndicator(closePrice, longSmaPeriod);

        EMAIndicator emaVal = new EMAIndicator(closePrice, shortSmaPeriod);
        RSIIndicator rsiVal = new RSIIndicator(closePrice, shortSmaPeriod);
        MACDIndicator macdValue = new MACDIndicator(closePrice);
        ROCIndicator rocValue = new ROCIndicator(closePrice, shortSmaPeriod);
        ParabolicSarIndicator sar = new ParabolicSarIndicator(series);
        StochasticOscillatorKIndicator stoK = new StochasticOscillatorKIndicator(series, shortSmaPeriod);
        StochasticOscillatorDIndicator stoD = new StochasticOscillatorDIndicator(stoK);
        CCIIndicator cci = new CCIIndicator(series, shortSmaPeriod);
        OnBalanceVolumeIndicator obv = new OnBalanceVolumeIndicator(series);
        WilliamsRIndicator williamsR = new WilliamsRIndicator(series, shortSmaPeriod);

        // Create the rules
        Rule buyingRule = new OverIndicatorRule(shortSma, longSma) // The short SMA crosses above the long SMA
                .and(new UnderIndicatorRule(rsiVal, 30)) // The RSI is under 30 (oversold)
                .and(new OverIndicatorRule(macdValue, 0)) // The MACD is positive
                .and(new OverIndicatorRule(rocValue, 0)) // The ROC is positive
                .and(new OverIndicatorRule(emaVal, 0)) // The EMA is positive
                .and(new OverIndicatorRule(sar, closePrice)) // The price crosses above the Parabolic SAR
                .and(new UnderIndicatorRule(stoK, 20)) // The Stochastic Oscillator K line is under 20
                .and(new UnderIndicatorRule(stoD, 20)) // The Stochastic Oscillator D line is under 20
                .and(new OverIndicatorRule(cci, -100)) // The CCI is above -100
                .and(new OverIndicatorRule(obv, 0)) // The OBV is rising
                .and(new UnderIndicatorRule(williamsR, -80)); // The Williams %R is under -80

        Rule sellingRule = new UnderIndicatorRule(shortSma, longSma) // The short SMA crosses below the long SMA
                .and(new OverIndicatorRule(rsiVal, 70)) // The RSI is over 70 (overbought)
                .and(new UnderIndicatorRule(macdValue, 0)) // The MACD is negative
                .and(new UnderIndicatorRule(rocValue, 0)) // The ROC is negative
                .and(new UnderIndicatorRule(emaVal, 0)) // The EMA is negative
                .and(new UnderIndicatorRule(sar, closePrice)) // The price crosses below the Parabolic SAR
                .and(new OverIndicatorRule(stoK, 80)) // The Stochastic Oscillator K line is over 80
                .and(new OverIndicatorRule(stoD, 80)) // The Stochastic Oscillator D line is over 80
                .and(new UnderIndicatorRule(cci, 100)) // The CCI is under 100
                .and(new UnderIndicatorRule(obv, 0)) // The OBV is falling
                .and(new OverIndicatorRule(williamsR, -20)); // The Williams %R is over -20

        // System.out.println(buyingRule);

        // System.out.println(sellingRule);

        Strategy strategy = new BaseStrategy(
        new CrossedUpIndicatorRule(shortSma, longSma),
        new CrossedDownIndicatorRule(shortSma, longSma));

        // Strategy strategy = new BaseStrategy(buyingRule, sellingRule);

        System.out.println(strategy);

        // Backtest the strategy
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        System.out.println("Number of trades: " + tradingRecord.getTradeCount());
        List<Trade> trades = tradingRecord.getTrades();
        for (int i = 0; i < trades.size(); i++) {
            Trade trade = trades.get(i);
            System.out.println("Trade " + i + ": " + trade);
        }
        
        // Print the total profit of our strategy

        TotalProfitCriterion totalProfit = new TotalProfitCriterion();
        NumberOfTradesCriterion numberOfTrades = new NumberOfTradesCriterion();
        NumberOfWinningTradesCriterion numberOfWinningTrades = new NumberOfWinningTradesCriterion();
        NumberOfLosingTradesCriterion numberOfLosingTrades = new NumberOfLosingTradesCriterion();
        AverageProfitableTradesCriterion avgProfitTrades = new AverageProfitableTradesCriterion();
        MaximumDrawdownCriterion maxDrawdown = new MaximumDrawdownCriterion();
        RewardRiskRatioCriterion rewardRiskRatio = new RewardRiskRatioCriterion();

        double totalProfitValue = totalProfit.calculate(series, tradingRecord).doubleValue();
        int totalTradesValue = numberOfTrades.calculate(series, tradingRecord).intValue();
        int winningTradesValue = numberOfWinningTrades.calculate(series, tradingRecord).intValue();
        int losingTradesValue = numberOfLosingTrades.calculate(series, tradingRecord).intValue();
        double winRateValue = ((double) winningTradesValue / totalTradesValue) * 100;
        double averageProfitValue = avgProfitTrades.calculate(series, tradingRecord).doubleValue();
        double maxDrawdownValue = maxDrawdown.calculate(series, tradingRecord).doubleValue();
        double sharpeRatioValue = rewardRiskRatio.calculate(series,
        tradingRecord).doubleValue();

        Num totalLoss = series.numOf(0);
        for (Trade trade : tradingRecord.getTrades()) {
            Num profit = totalProfit.calculate(series, trade);
            if (profit.isLessThanOrEqual(series.numOf(1))) {
                totalLoss = totalLoss.plus(series.numOf(1).minus(profit));
            }
        }

        // Compute number of losing trades
        int noOfLosingTrades = numberOfLosingTrades.calculate(series, tradingRecord).intValue();

        // Compute average loss per losing trade
        double averageLossPerLosingTrade = totalLoss.dividedBy(series.numOf(noOfLosingTrades)).doubleValue();

        System.out.println("Total trades: " + totalTradesValue);
        System.out.println("Winning trades: " + winningTradesValue);
        System.out.println("Losing trades: " + losingTradesValue);
        System.out.println("Win rate: " + winRateValue + "%");
        System.out.println("Average profit per winning trade: " + averageProfitValue + "%");
        System.out.println("Average loss per losing trade: " + averageLossPerLosingTrade + "%");
        System.out.println("Maximum drawdown: " + maxDrawdownValue + "%");
        System.out.println("Sharpe ratio: " + sharpeRatioValue);
        System.out.println("Total profit: " + totalProfitValue + "%");

    }
}