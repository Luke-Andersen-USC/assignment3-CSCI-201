package restaurantClasses;

public class Order {
	int time;
	String restaurant;
	String menuItem;
	
	Order(int time, String restaurant, String menuItem)
	{
		this.time = time;
		this.restaurant = restaurant;
		this.menuItem = menuItem;
	}
	
	public int getTime()
	{
		return time;
	}

	public String getRestaurant()
	{
		return restaurant;
	}
	
	public String getMenuItem()
	{
		return menuItem;
	}
}
