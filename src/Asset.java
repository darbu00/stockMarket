
public class Asset {
	
	private Stock stock;
	private int quantity;
	private double value;
	
	
	public Asset(Stock stock, int quantity) {
		this.stock = stock;
		this.quantity = quantity;
		this.value = (int)(100.0d * (quantity * stock.getCurrentPrice()) + 0.5) / 100.0d;
	}
	
	public Stock getStock() {
		return stock;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
		//This guarantees that value is current in the asset
		this.value = formatDouble(this.getStock().getCurrentPrice() * this.quantity);
	}

	public double getValue() {
		//This guarantees that value is current in the asset
		this.setValue(formatDouble(this.getStock().getCurrentPrice() * this.quantity));
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "Asset [stock=" + stock + ", quantity=" + quantity + ", value=" + this.getValue() + "]";
	}

	private double formatDouble(double number){
		return ((int)(100.0d * number + 0.5) / 100.0d);
	}

}
