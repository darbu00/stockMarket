
public class Stock {
	
	private final String name;
	private final String shortName;
	private double currentPrice;
	private double previousPrice;
	private double priceChange;

	
	public Stock(String name, String shortName, double currentPrice) {
		this.name = name;
		this.shortName = shortName;
		this.currentPrice = currentPrice;
		this.previousPrice = currentPrice;
	}


	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public double getPreviousPrice() {
		return previousPrice;
	}

	public void setPreviousPrice(double previousPrice) {
		this.previousPrice = previousPrice;
	}
	
	public double getPriceChange() {
		return priceChange;
	}
	
	public void setPriceChange(double priceChange) {
		this.priceChange = priceChange;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	
	@Override
	public String toString() {
		return "Stock [name=" + name + ", shortName=" + shortName + ", currentPrice=" + currentPrice + ", priceChange="
				+ priceChange + "]";
	}
	
}
