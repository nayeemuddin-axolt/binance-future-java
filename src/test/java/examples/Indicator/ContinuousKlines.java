package examples.Indicator;

import org.json.JSONArray;
import org.json.JSONObject;

public class ContinuousKlines {
    private long timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private long closeTimestamp;
    private double quoteAssetVolume;
    private double numTrades;
    private double takerBuyBaseAssetVolume;
    private double takerBuyQuoteAssetVolume;
    private double ignore;

    public ContinuousKlines(long timestamp, double open, double high, double low, double close, double volume,
            long closeTimestamp, double quoteAssetVolume, double numTrades, double takerBuyBaseAssetVolume,
            double takerBuyQuoteAssetVolume, double ignore) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.closeTimestamp = closeTimestamp;
        this.quoteAssetVolume = quoteAssetVolume;
        this.numTrades = numTrades;
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
        this.ignore = ignore;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public double getVolume() {
        return volume;
    }

    public long getCloseTimestamp() {
        return closeTimestamp;
    }

    public double getQuoteAssetVolume() {
        return quoteAssetVolume;
    }

    public double getNumTrades() {
        return numTrades;
    }

    public double getTakerBuyBaseAssetVolume() {
        return takerBuyBaseAssetVolume;
    }

    public double getTakerBuyQuoteAssetVolume() {
        return takerBuyQuoteAssetVolume;
    }

    public double getIgnore() {
        return ignore;
    }
}
