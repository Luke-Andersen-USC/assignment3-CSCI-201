package restaurantClasses;
import java.io.BufferedReader;
import java.io.File;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.net.ServerSocket;
import java.net.Socket;

public class JoesTablesServer {

	private static Scanner scanner;
	private static Vector<Order> orders = new Vector<Order>();
	private static Restaurants restaurants = new Restaurants();
	private static Double userLat = 0.0;
	private static Double userLong = 0.0;
	
	private static int driversReq = 0;
	private static int driversCur = 0;
	private static int portNum = 3456;
	
	private static Vector<ServerThread> serverThreads = new Vector<ServerThread>();
	private static ServerSocket ss = null;
	private static Socket s = null;
	
	private static Vector<Order> toGo = new Vector<Order>();
	private static boolean ordersReady = false;
	
	
	public static void main(String []args)
	{
		//SET UP
		scanner = new Scanner(System.in);
		
		LoadScheduleFile();
		GetUserLocation();
		GetDriverCount();
		LoadRestaurants();
		
		//END OF USER INPUT - START OF METHOD
		
		new JoesTablesServer();
		
		scanner.close();	
		
	}
	
	//END OF MAIN
	

	private static void LoadScheduleFile()
	{
		boolean goodFile = false;
		String temp = "";
		String schedFileName = "";
	
		while(!goodFile)
		{
			System.out.println("What is the name of the file containing the schedule information?");
			
			schedFileName = scanner.nextLine();
			
			System.out.println(schedFileName);
			Scanner sc = null;
			
			try
			{
				BufferedReader br = new BufferedReader(new FileReader(new File(schedFileName)));
				goodFile = true;
				
	
				while((temp = br.readLine()) != null)
				{					
					int time = Integer.parseInt(temp.substring(0, temp.indexOf(',')));
					String restaurant = (temp.substring(temp.indexOf(',') + 1,temp.lastIndexOf(',')));
					String menuItem = temp.substring(temp.lastIndexOf(',') + 1);
					Order order = new Order(time, restaurant.trim(), menuItem.trim());
					orders.add(order);
				}
				
				br.close();
			}
			catch(FileNotFoundException e)
			{
				System.out.println("The file " + schedFileName + " does not exist");
			}
			catch(IOException | JsonIOException e)
			{
				System.out.println("The file " + schedFileName + " could not be read");
			}
			catch(JsonSyntaxException e)
			{
				System.out.println("The file " + schedFileName + " is not formatted properly");
			}
			finally
			{
				if(sc != null)
				{
					sc.close();	
				}
				
			}
			
		}
	}
	
	//END OF LOAD SCHEDULE
	
	
	private static void GetUserLocation()
	{
		boolean userLatIn = false;
		while(!userLatIn)
		{
			System.out.println("What is your latitude?");	
		
			try
			{
				userLat = Double.parseDouble(scanner.nextLine());
				userLatIn = true;
			}
			catch(NumberFormatException e)
			{
				System.out.println("That is not a valid option.");
				userLatIn = false;
			}
		}
		
		
		boolean userLongIn = false;
		while(!userLongIn)
		{
			System.out.println("What is your longitude?");	
		
			try
			{
				userLong = Double.parseDouble(scanner.nextLine());
				userLongIn = true;
			}
			catch(NumberFormatException e)
			{
				System.out.println("That is not a valid option.");
				userLongIn = false;
			}
		}
		
		ServerThread.SetUserLoc(userLat, userLong);
		
	}
	
	//END OF GET USER LOCATION
	
	
	private static void GetDriverCount()
	{
		boolean driversIn = false;
		while(!driversIn)
		{
			System.out.println("How many drivers will be in service today?");	
		
			try
			{
				driversReq = Integer.parseInt(scanner.nextLine());
				driversIn = true;
			}
			catch(NumberFormatException e)
			{
				System.out.println("That is not a valid option.");
				driversIn = false;
			}
		}
	}
	
	//END OF GET DRIVER COUNT
	
	private static void LoadRestaurants()
	{
		
		for(int i = 0; i < orders.size(); i++)
		{
			String restName = orders.get(i).getRestaurant();
			
			restName = restName.replaceAll(" ", "-");
			restName = restName.replaceAll("’", "'");
			
			URL yelp;
			try {
				
				yelp = new URL("https://api.yelp.com/v3/businesses/search?term=" + restName + "&latitude=" + Double.toString(userLat) + "&longitude=" + Double.toString(userLong));
				HttpURLConnection hConnect = (HttpURLConnection)yelp.openConnection();
				
				hConnect.setRequestMethod("GET");
				hConnect.setRequestProperty("Authorization","Bearer tuhIoOM0lAK2oc_3xsVh1d3dbuGK67zxr_Cqm9FR-MByw9b4YU0ia_8qvWGji-LeC0kXthS9m_wWPIgNLxtUTQAnISfoGfVTyYV42dgOI4jtbx7QmTSn6rN1JMk5ZXYx");
				
				int responseCode = hConnect.getResponseCode();
				//System.out.println("GET Response Code :: " + responseCode);
				if (responseCode == HttpURLConnection.HTTP_OK) { // success
					BufferedReader in = new BufferedReader(new InputStreamReader(hConnect.getInputStream()));
					String inputLine;
					StringBuffer restInfo = new StringBuffer();
	
					while ((inputLine = in.readLine()) != null) {
						restInfo.append(inputLine);
					}
					in.close();
					
					String restInfoStr = restInfo.toString();
					
					//System.out.println(restInfo.toString());
					
					
					
					int rLatStart = restInfoStr.indexOf("latitude") + 10;
					int rLatEnd = restInfoStr.indexOf("longitude") - 3;
					Double restLat = Double.parseDouble(restInfoStr.substring(rLatStart, rLatEnd));
					
					int rLongStart = restInfoStr.indexOf("longitude") + 11;
					int rLongEnd = restInfoStr.indexOf("transactions") - 4;
					Double restLong = Double.parseDouble(restInfoStr.substring(rLongStart, rLongEnd));
					
					restName = orders.get(i).getRestaurant();
					
					if(!restaurants.restaurantExists(restName))
					{
						restaurants.add(new Restaurant(restName, restLat, restLong));
					}
					
				} 
				else 
				{
					System.out.println("GET request did not work.");
				}
			
			}
			catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
	}
	
	//END OF LOAD RESTAURANTS
	
	public JoesTablesServer()
	{
		
		try
		{
			ss = new ServerSocket(portNum);
			System.out.println("Listening on port " + portNum + ".");
			System.out.println("Waiting for drivers...");
			int threadNum = 1;
			while(driversReq > driversCur)
			{
				s = ss.accept();
				driversCur++;
				
				System.out.println("Connection from " + s.getInetAddress());
							
				serverThreads.add(new ServerThread(s, this, threadNum));
				threadNum++;
				
				int moreNeeded = driversReq - driversCur;
				if(moreNeeded > 0)
				{
					System.out.println("Waiting for " + moreNeeded + " more driver(s)");
				}
			}
			
			//Starting all serverThreads
			for(int i = 0; i < serverThreads.size(); i++)
			{
				serverThreads.get(i).start();
			}
			
			System.out.println("Starting Service.");
			
			
			RunSchedule();
		}
		
		catch(IOException ioe)
		{
			System.out.println("ioe in JoesTablesServer: " + ioe.getMessage());
		}
		finally
		{
			//Waiting until all threads are finished
			//System.out.println("Waiting for threads to stop!");
			while(true)
			{
				boolean finished = true;
				for(int i = 0; i < serverThreads.size(); i++)
				{
					if(!serverThreads.get(i).getAvailable())
					{
						finished = false;
					}
				}
				if(finished)
				{
					break;
				}
			}
			//System.out.println("Threads have stopped!");
			
			for(int i = 0; i < serverThreads.size(); i++)
			{
				serverThreads.get(i).SendUserDone();
			}
			try
			{
				//System.out.println("Closing Socket!");
				/*
				if(s != null)
				{
					s.close();
				}
				*/
				if(ss != null)
				{
					ss.close();
				}
			}
			catch(IOException ioe)
			{
				System.out.println("ioe in closing server socket: " + ioe.getMessage());
			}
		}
		
	}
	
	//END OF JOES TABLES SERVER CONSTRUCTOR
	
	private void RunSchedule()
	{
		//System.out.println("Starting Schedule Run!");
		
		boolean done = false;
		long start = System.currentTimeMillis();
		
		int i = 0;
		long elapsedTime = System.currentTimeMillis() - start;
		while(i < orders.size())
		{
			while(elapsedTime / 1000 >= orders.get(i).getTime())
			{
				//System.out.println("Adding order to goTo!");
				
				toGo.add(orders.get(i));
				i++;
				if(i >= orders.size())
				{
					done = true;
					break;
				}
				
			}
			if(!toGo.isEmpty())
			{
				//iterate through each server thread (based on bool availibility, assign order to serverthread
				
				boolean ordersDispatched = false;
				while(!ordersDispatched)
				{
					for(int j = 0; j < serverThreads.size(); j++)
					{
						if(serverThreads.get(j).getAvailable())
						{
							serverThreads.get(j).DeliverOrders(toGo);
							ordersDispatched = true;
							break;
						}
					}
				}
				//System.out.println("Orders dispatched!");
				toGo.clear();
			}
			if(done)
			{
				break;
			}
			elapsedTime = System.currentTimeMillis() - start;
			
		}
		
		System.out.println("All orders completed!");
	}
	
	
	
	//END OF RUN SCHEDULE
	
	public int GetReqDrivers()
	{
		return driversReq;
	}
	
	public int GetCurDrivers()
	{
		return driversCur;
	}
	
	public int GetPortNumber()
	{
		return portNum;
	}
	
	public Restaurants GetRestaurants()
	{
		return restaurants;
	}
	
	
	
	
	


}




