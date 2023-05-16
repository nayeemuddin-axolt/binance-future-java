package examples.cm_futures.market;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MarkPrice {
    private MarkPrice() {
    }

    private static final Logger logger = LoggerFactory.getLogger(MarkPrice.class);

    public static String main(String[] args) {
        String symbol;

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        if (args.length == 0) {
            logger.error("Symbol argument is missing");
            return null;
        } else {
            symbol = args[0];
            parameters.put("symbol", symbol);
        }

        CMFuturesClientImpl client = new CMFuturesClientImpl();

        try {
            String result = client.market().markPrice(parameters);
            logger.info(result);
            return result;
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }
        return null;
    }
}