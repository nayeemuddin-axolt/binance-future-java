package examples.Indicator;

import java.util.List;

public class OrderBook {
    private long lastUpdateId;

  /**
   * List of bids (price/qty).
   */
  private List<OrderBookEntry> bids;

  /**
   * List of asks (price/qty).
   */
  private List<OrderBookEntry> asks;

  public long getLastUpdateId() {
    return lastUpdateId;
  }

  public void setLastUpdateId(long lastUpdateId) {
    this.lastUpdateId = lastUpdateId;
  }

  public List<OrderBookEntry> getBids() {
    return bids;
  }

  public void setBids(List<OrderBookEntry> bids) {
    this.bids = bids;
  }

  public List<OrderBookEntry> getAsks() {
    return asks;
  }

  public void setAsks(List<OrderBookEntry> asks) {
    this.asks = asks;
  }
}
