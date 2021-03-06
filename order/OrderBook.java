
package pkg.order;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.market.MarketHistory;
import pkg.market.api.IObserver;
import pkg.market.api.PriceSetter;
import pkg.util.OrderUtility;
import java.util.Map.Entry;
import pkg.trader.Trader;

public class OrderBook implements Comparator<Order> {
	Market market;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;
	ArrayList<Order> orderList;

	public OrderBook(Market market) {
		this.market = market;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
		orderList = new ArrayList<Order>();
	}

	public void addToOrderBook(Order order) {
		if (order instanceof BuyOrder) {
			if (buyOrders.get(order.getStockSymbol()) != null) {
				orderList = buyOrders.get(order.getStockSymbol());
				orderList.add(order);
			} else {
				orderList = new ArrayList<Order>();
				orderList.add(order);
			}
			buyOrders.put(order.getStockSymbol(), orderList);
		} else {
			if (sellOrders.get(order.getStockSymbol()) != null) {
				orderList = sellOrders.get(order.getStockSymbol());
				orderList.add(order);
			} else {
				orderList = new ArrayList<Order>();
				orderList.add(order);
			}
			sellOrders.put(order.getStockSymbol(), orderList);
		}

	}

	public int compare(Order order1, Order order2) {
		if (order1.getPrice() == order2.getPrice()) {
			return 0;
		}
		return order1.getPrice() > order2.getPrice() ? 1 : -1;
	}

	public void trade()  {
		String mSymbol = "";
		@SuppressWarnings("unchecked")
		ArrayList<Order> temp = (ArrayList<Order>) orderList.clone();
		Iterator<Order> it = temp.iterator();
          	//iterate through list of orders and perform trades
		while (it.hasNext()){
			Order stockSymbol = it.next();	
			if(mSymbol != stockSymbol.getStockSymbol()){
				mSymbol = stockSymbol.getStockSymbol();
				ArrayList<Order> buyOrdersArray = buyOrders.get(mSymbol);
				ArrayList<Order> sellOrdersArray = sellOrders.get(mSymbol);
				HashMap<Double, Double> buyMap = new HashMap<Double, Double>();
				HashMap<Double, Double> sellMap = new HashMap<Double, Double>();
				double buy = 0;
				double sell = 0;
				double maxNum = 0;
				double maxPrice = 0;
				PriceSetter priceSetter = new PriceSetter();
				priceSetter.registerObserver(m.getMarketHistory());
				m.getMarketHistory().setSubject(priceSetter);
				Collections.sort(buyOrdersArray, this);
				Collections.sort(sellOrdersArray, this);
				Collections.reverse(sellOrdersArray);
				
                          	ArrayList<Double> prices = getPrices(buyOrdersArray, sellOrdersArray);
				if (buyOrdersArray.get(i) != null){
					buy += buyOrdersArray.get(i).getSize();
					buyMap.put(buyOrdersArray.get(i).getPrice(), buy);
				}
				if (sellOrdersArray.get(i) != null){
					sell += sellOrdersArray.get(i).getSize();
					sellMap.put(sellOrdersArray.get(i).getPrice(), sell);
				}
                          
				for (int j = 0; j < prices.size(); j++){
					buy = buyMap.get(prices.get(j));
					if(sellMap.containsKey(prices.get(j))){
						sell = sellMap.get(prices.get(j));
					}
					if(buy > sell){
						if (sell > maxNum){
							maxNum = sell;
							maxPrice = prices.get(j);
						}
					}
					else {
						if (buy > maxNum){
							maxNum = buy;
							maxPrice = prices.get(j);

						}
					}
				}

				priceSetter.setNewPrice(m, buyOrdersArray.get(0).getStockSymbol(), maxPrice);
				
				performTrades(buyOrdersArray, sellOrdersArray, maxPrice);
			}
		}
	}
  
	private void performTrades(ArrayList<Order> buyOrdersArray, ArrayList<Order> sellOrdersArray, double maxPrice) {
		try{
			for(int k = 0; k < buyOrdersArray.size(); k++){
				if (buyOrdersArray.get(k).getPrice() >= maxPrice){
					Trader trader = buyOrdersArray.get(k).getTrader();
					if (trader != null)
					trader.tradePerformed(buyOrdersArray.get(k), maxPrice);
				} else if (buyOrdersArray.get(k).isMarketOrder){
					Trader trader = buyOrdersArray.get(k).getTrader();
					if (trader != null)
					trader.tradePerformed(buyOrdersArray.get(k), maxPrice);
							
				}
						
			}
			for (int k = 0;  k < sellOrdersArray.size(); k ++){
				if (sellOrdersArray.get(k).getPrice() <= maxPrice){
					Trader trader = sellOrdersArray.get(k).getTrader();
					if (trader != null)
					trader.tradePerformed(sellOrdersArray.get(k), maxPrice);
				} else if (sellOrdersArray.get(k).isMarketOrder){
					Trader trader = sellOrdersArray.get(k).getTrader();
					if (trader != null)
					trader.tradePerformed(sellOrdersArray.get(k), maxPrice);
					
				}
			}
		}catch(StockMarketExpection e){
			e.printStackTrace();
		}
	}
  
  	private ArrayList<Double> getPrices(ArrayList<Order> buyOrdersArray, ArrayList<Order> sellOrdersArray) {
		ArrayList<Double> prices = new ArrayList<Double>();
          	for (int i = 0; i < buyOrdersArray.size() && i < sellOrdersArray.size(); i++){
			if (buyOrdersArray.get(i).getPrice() >= sellOrdersArray.get(i).getPrice()){
				prices.add(buyOrdersArray.get(i).getPrice());
			}
			else {
				prices.add(sellOrdersArray.get(i).getPrice());
			}
		}
          	return prices;
  	}
}



