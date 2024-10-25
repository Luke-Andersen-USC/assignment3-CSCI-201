package restaurantClasses;
import java.util.Vector;

public final class Restaurant {

	String name;
	Double latitude;
	Double longitude;
	
	Restaurant(String name, Double latitude, Double longitude)
	{
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String newName)
	{
		name = newName;
	}
	
	public Double getLatitude()
	{
		return latitude;
	}
	
	public void setLatitude(Double newLatitude)
	{
		latitude = newLatitude;
	}
	
	public Double getLongitude()
	{
		return longitude;
	}
	
	public void setLongitude(Double newLongitude)
	{
		longitude = newLongitude;
	}

}
