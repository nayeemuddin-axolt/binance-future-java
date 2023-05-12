package examples.cm_futures.account;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import examples.PrivateConfig;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class ModifyOrder {
    private ModifyOrder() {
    }
    private static final double price = 0.97010;
    private static final Integer orderId = 34349139;
    private static final double quantity = 16.00000000;

    private static final Logger logger = LoggerFactory.getLogger(ModifyOrder.class);

    public static void main(String[] args) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();       

        parameters.put("orderId", orderId);
        parameters.put("symbol", "ADAUSD_PERP");
        parameters.put("side", "SELL");
        parameters.put("price", price);        
        parameters.put("quantity", quantity);

        CMFuturesClientImpl client = new CMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY, PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);
        
        try {
            String result = client.account().modifyOrder(parameters);
            logger.info(result);
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }
    }
}
