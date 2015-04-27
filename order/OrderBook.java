
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
	Market m;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;
	ArrayList<Order> mArray;

	public OrderBook(Market m) {
		this.m = m;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
		mArray = new ArrayList<Order>();
	}

	public void addToOrderBook(Order order) {
		if (order instanceof BuyOrder) {
			if (buyOrders.get(order.getStockSymbol()) != null) {
				mArray = buyOrders.get(order.getStockSymbol());
				mArray.add(order);
			} else {
				mArray = new ArrayList<Order>();
				mArray.add(order);
			}
			buyOrders.put(order.getStockSymbol(), mArray);
		} else {
			if (sellOrders.get(order.getStockSymbol()) != null) {
				mArray = sellOrders.get(order.getStockSymbol());
				mArray.add(order);
			} else {
				mArray = new ArrayList<Order>();
				mArray.add(order);
			}
			sellOrders.put(order.getStockSymbol(), mArray);
		}

	}

	public int compare(Order o1, Order o2) {
		if (o1.getPrice() == o2.getPrice()) {
			return 0;
		}
		return o1.getPrice() > o2.getPrice() ? 1 : -1;
	}

	public void trade()  {
		String mSymbol = "";
		@SuppressWarnings("unchecked")
		ArrayList<Order> temp = (ArrayList<Order>) mArray.clone();
		Iterator<Order> it = temp.iterator();
		while (it.hasNext()){
			Order stockSymbol = it.next();	
			if(mSymbol != stockSymbol.getStockSymbol()){
				mSymbol = stockSymbol.getStockSymbol();
				ArrayList<Order> buyOrdersArray = buyOrders.get(mSymbol);
				ArrayList<Order> sellOrdersArray = sellOrders.get(mSymbol);
				HashMap<Double, Double> buyMap = new HashMap<Double, Double>();
				HashMap<Double, Double> sellMap = new HashMap<Double, Double>();
				ArrayList<Double> prices = new ArrayList<Double>();
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
				
		
				for (int i = 0; i < buyOrdersArray.size() && i < sellOrdersArray.size(); i++){
					if (buyOrdersArray.get(i).getPrice() >= sellOrdersArray.get(i).getPrice()){
						prices.add(buyOrdersArray.get(i).getPrice());
					}
					else {
						prices.add(sellOrdersArray.get(i).getPrice());
					}
					if (buyOrdersArray.get(i) != null){
						buy += buyOrdersArray.get(i).getSize();
						buyMap.put(buyOrdersArray.get(i).getPrice(), buy);
					}
					if (sellOrdersArray.get(i) != null){
						sell += sellOrdersArray.get(i).getSize();
						sellMap.put(sellOrdersArray.get(i).getPrice(), sell);
					}
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
		}
	}
}



