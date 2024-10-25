package restaurantClasses;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Util{
	
	public static String getElapsedTime(long start)
	{
		DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss.SSS]");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String time = dateFormat.format(System.currentTimeMillis() - start); //2016/11/16 12:08:43
		
		return time;
	}
}
