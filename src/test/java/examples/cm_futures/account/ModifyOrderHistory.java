package examples.cm_futures.account;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import examples.PrivateConfig;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModifyOrderHistory {
    private ModifyOrderHistory() {
    }

    private static final Integer orderId = 34705033;
    
    private static final Logger logger = LoggerFactory.getLogger(ModifyOrder.class);

    public static void main(String[] args) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
    
        parameters.put("orderId", orderId);
        parameters.put("symbol", "ADAUSD_PERP");
        parameters.put("side", "SELL");

        CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY, PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);
        
        try {
            String result = client.account().orderModifyHistory(parameters);
            logger.info(result);
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }
    }
}
