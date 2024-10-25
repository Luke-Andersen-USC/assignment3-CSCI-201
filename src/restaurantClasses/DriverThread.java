package restaurantClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Math;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DriverThread extends Thread {
	public static double UserLat;
	public static double UserLong;
	public static long StartTime;
	public static long ServerStartTime;
	
	private double myLat;
	private double myLong;
	
	private Vector<String> myMenuItems = new Vector<String>();
	private Restaurants myRestaurants = new Restaurants();
	
	private BufferedReader br;
	private PrintWriter pw;
	
	private Scanner scanner;
	
	private Socket s = null;
	
	private boolean firstOrder = true;
	private boolean secondLeg = false;
	private boolean allDone = false;
	
	public DriverThread()
	{
		//USER INPUT
		scanner = new Scanner(System.in);
		
		System.out.println("Welcome to JoesTables v2.0!");
		
		System.out.println("Enter the server hostname:");
		
		String hostName = scanner.nextLine();
		
		System.out.println("Enter the server port:");
		
		int port = Integer.parseInt(scanner.nextLine());
		
		
		try {
			s = new Socket(hostName, port);
			
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
		}
		catch (UnknownHostException e) {
			
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		//FINISHED CONNECTION
		
		//System.out.println("Reading messages!");
		
		String messageRead;
		do
		{
			messageRead = "No message!";
			try {
				messageRead = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//System.out.println("From ServerThread: " + messageRead);
			
			//Initially setting User Location
			if(messageRead.substring(0, 8).equals("User_Loc"))
			{
				//System.out.println("Setting User Location for Driver Threads from: " + messageRead);
				
				messageRead = messageRead.substring(messageRead.indexOf(',') + 1);
				double uLat = Double.parseDouble(messageRead.substring(0, messageRead.indexOf(',')));
				
				messageRead = messageRead.substring(messageRead.indexOf(',') + 1);
				double uLong = Double.parseDouble(messageRead);
				
				setUserLocation(uLat, uLong);
				
				//System.out.println("Driver User Lat: " + UserLat);
				//System.out.println("Driver User Long: " + UserLong);
				
			} //Setting up User Time
			else if(messageRead.substring(0, 9).equals("User_Time"))
			{
				//System.out.println("Setting User Time for Driver Threads from: " + messageRead);
				
				messageRead = messageRead.substring(messageRead.indexOf(',') + 1);
				StartTime = Long.parseLong(messageRead);
				
				//System.out.println("Driver for Start Time: " + StartTime);
			}
			else
			{
				System.out.println(messageRead);
				if(messageRead.equals("All drivers have arrived!"))
				{
					break;
				}
				
			}
			
		}
		while(!messageRead.equals("All drivers have arrived!"));
		
		ServerStartTime = System.currentTimeMillis();
		
		this.start();
		
		scanner.close();
	}
	
	public static void main(String []args) throws NumberFormatException, IOException
	{
		new DriverThread();
	}
	
	public void run()
	{
		//StartTime = System.currentTimeMillis();
		//System.out.println("Starting Driver Thread run!");
		while(true)
		{
			//System.out.println("Starting GetOrders!");
			GetOrders();
			if(allDone)
			{
				break;
			}
			
			while(!myMenuItems.isEmpty())
			{
				TravelNearest();
			}
			ReturnToHQ();
			
			
		}
	
	
	}
	
	
	
	private double DistanceCalc(double lat1, double long1, double lat2, double long2)
	{
		double dist = 3963.0f * Math.acos(Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + 
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(long2 - long1)) );
		
		return dist;
	}
	
	private void TravelNearest()
	{
		//Select nearest
		int minIndex = 0;
		Restaurant rest = myRestaurants.data.get(0);
		double minDist = DistanceCalc(myLat, myLong, rest.getLatitude(), rest.getLongitude());
		
		for(int i = 1; i < myRestaurants.data.size(); i++)
		{
			rest = myRestaurants.data.get(i);
			double dist = DistanceCalc(myLat, myLong, rest.getLatitude(), rest.getLongitude());
			if(dist < minDist)
			{
				minDist = dist;
				minIndex = i;
			}
			
		}
		
		String restName = myRestaurants.data.get(minIndex).getName();
		
		if(secondLeg)
		{
			System.out.println(Util.getElapsedTime(StartTime) +  " Continuing delivery to " + restName + ".");
		}
		
		try 
		{
			//System.out.println("Driver traveling " + minDist);
			Thread.sleep((long) (minDist * 1000.0f));
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		
		System.out.println(Util.getElapsedTime(StartTime) +  " Finished delivery of " + myMenuItems.get(minIndex) + " from " + restName + "!");
		
		myLat = myRestaurants.data.get(minIndex).getLatitude();
		myLong = myRestaurants.data.get(minIndex).getLongitude();
		
		myRestaurants.data.remove(minIndex);
		myMenuItems.remove(minIndex);
		
		for(int i = 0; i < myRestaurants.data.size(); i++)
		{
			if(restName.equals(myRestaurants.data.get(i).getName()))
			{
				System.out.println(Util.getElapsedTime(StartTime) +  " Finished delivery of " + myMenuItems.get(i) + " from " + restName + "!");
				myRestaurants.data.remove(minIndex);
				myMenuItems.remove(minIndex);
				i--;
			}
		}
		
		secondLeg = true;
	}
	
	private void ReturnToHQ()
	{
		System.out.println(Util.getElapsedTime(StartTime) + " Finished all deliveries, returning back to HQ.");
		
		double dist = DistanceCalc(myLat, myLong, UserLat, UserLong);
		
		try 
		{
			Thread.sleep((long) (dist * 1000.0f));
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		myLat = UserLat;
		myLong = UserLong;
		
		System.out.println(Util.getElapsedTime(StartTime) + " Returned to HQ.");
		
		pw.println("Driver has returned!");
		pw.flush();
		
	}
	
	private void GetOrders()
	{
		secondLeg = false;
		
		String ordersRecieved;
		try {
			ordersRecieved = br.readLine();
			
			if(firstOrder)
			{
				StartTime = System.currentTimeMillis();
				firstOrder = false;
			}
			
			if(ordersRecieved.equals("All orders completed!"))
			{
				System.out.println(Util.getElapsedTime(ServerStartTime) + " " + ordersRecieved);
				allDone = true;
				return;
			}
			
			while(true)
			{
				String name = ordersRecieved.substring(0, ordersRecieved.indexOf(','));
				ordersRecieved = ordersRecieved.substring(ordersRecieved.indexOf(',') + 1);
				
				String menuItem = ordersRecieved.substring(0, ordersRecieved.indexOf(','));
				ordersRecieved = ordersRecieved.substring(ordersRecieved.indexOf(',') + 1);
				
				Double latitude = Double.parseDouble(ordersRecieved.substring(0, ordersRecieved.indexOf(',')));
				ordersRecieved = ordersRecieved.substring(ordersRecieved.indexOf(',') + 1);
				
				Double longitude = 0.0;
				
				if(ordersRecieved.indexOf(',') != -1)
				{
					longitude = Double.parseDouble(ordersRecieved.substring(0, ordersRecieved.indexOf(',')));
					ordersRecieved = ordersRecieved.substring(ordersRecieved.indexOf(',') + 1);
				}
				else //last order
				{
					longitude = Double.parseDouble(ordersRecieved);
					
					//System.out.println("Driver adding [" + name + "] [" + menuItem + "] [" + latitude + "] [" + longitude + "]");
					
					myRestaurants.add(new Restaurant(name, latitude, longitude));
					myMenuItems.add(menuItem);
					
					break;
				}
				
				//System.out.println("Driver adding [" + name + "] [" + menuItem + "] [" + latitude + "] [" + longitude + "]");
				
				myRestaurants.add(new Restaurant(name, latitude, longitude));
				myMenuItems.add(menuItem);
			}	
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!secondLeg)
		{
			for(int i = 0; i < myRestaurants.data.size(); i++)
			{
				System.out.println(Util.getElapsedTime(StartTime) + " Starting delivery of " + myMenuItems.get(i) + " from " + myRestaurants.data.get(i).getName() + "!");
			}
		}
		
		
		myLat = UserLat;
		myLong = UserLong;
	
	}
	
	public static void setUserLocation(double uLat, double uLong)
	{
		UserLat = uLat;
		UserLong = uLong;
	}
	
	public static void setStartTime(long sTime)
	{
		StartTime = sTime;
	}
	
	
	
	
}
