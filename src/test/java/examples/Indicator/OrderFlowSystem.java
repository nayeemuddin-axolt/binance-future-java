package examples.Indicator;

public interface OrderFlowSystem {

    void placeBuyOrder(int timeFrame, String lowestPrice);

    void placeSellOrder(int timeFrame, String highestPrice);

    void holdOrder(int timeFrame);
}
