// package examples.Indicator;

// import org.ta4j.core.Bar;
// import org.ta4j.core.BaseBarSeries;
// import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
// import org.ta4j.core.indicators.SMAIndicator;
// import com.binance.api.client.BinanceApiClientFactory;
// import com.binance.api.client.BinanceApiRestClient;
// import com.binance.api.client.domain.market.Candlestick;
// import com.binance.api.client.domain.market.CandlestickInterval;

// // Set up the Binance API client
// BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, secret);
// BinanceApiRestClient client = factory.newRestClient();

// // Fetch historical candlestick data for a symbol and interval
// List<Candlestick> candlesticks = client.getCandlestickBars(symbol, CandlestickInterval.ONE_HOUR);

// // Convert the candlestick data into a BarSeries object
// BaseBarSeries series = new BaseBarSeries();
// for (Candlestick candlestick : candlesticks) {
//     ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(candlestick.getCloseTime()), ZoneId.systemDefault());
//     BigDecimal openPrice = new BigDecimal(candlestick.getOpen());
//     BigDecimal highPrice = new BigDecimal(candlestick.getHigh());
//     BigDecimal lowPrice = new BigDecimal(candlestick.getLow());
//     BigDecimal closePrice = new BigDecimal(candlestick.getClose());
//     BigDecimal volume = new BigDecimal(candlestick.getVolume());
//     series.addBar(new BaseBar(Duration.ofHours(1), endTime, openPrice, highPrice, lowPrice, closePrice, volume));
// }

// // Create the ClosePriceIndicator and SMAIndicator using the series and desired time frame
// ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
// SMAIndicator sma = new SMAIndicator(closePrice, timeFrame);

