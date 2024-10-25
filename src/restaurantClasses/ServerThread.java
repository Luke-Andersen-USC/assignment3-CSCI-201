package restaurantClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

public class ServerThread extends Thread {

	private JoesTablesServer server;
	
	private Vector<Order> myOrders;
	
	private PrintWriter pw;
	private BufferedReader br;
	boolean enoughDrivers = false;
	
	private static Double UserLat = 0.0;
	private static Double UserLong = 0.0;
	
	private Socket socket;
	
	private boolean available = true;
	private int threadNum = -1;
	
	private boolean programEnd = false;
	
	public ServerThread(Socket s, JoesTablesServer j, int num)
	{
		threadNum = num;
		//System.out.println("Starting ServerThread Constructor");
		try
		{
			socket = s;
			server = j;
			pw = new PrintWriter(s.getOutputStream());
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			SendUserLoc();
			SendUserStartTime();
			
			
			if(server.GetCurDrivers() < server.GetReqDrivers())
			{
				SendEnoughDrivers();
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		//System.out.println("Thread " + threadNum + "Done with stuff! Will wait!");
		
	}
	
	private void SendEnoughDrivers()
	{
		//System.out.println("Thread " + threadNum + " sending Enough Drivers");
		if(server.GetCurDrivers() < server.GetReqDrivers())
		{
			int driversNeeded = server.GetReqDrivers() - server.GetCurDrivers();
			String line = driversNeeded + " more driver(s) is needed before the service can begin...";
			
			pw.println(line);
			pw.flush();
			
			//System.out.println("Thread " + threadNum + " sending: " + line);
		}
		else
		{
			String line = "All drivers have arrived!";
			
			pw.println(line);
			pw.flush();
			enoughDrivers = true;
			
			//System.out.println("Thread " + threadNum + " sending: " + line);
		}
	}
	
	private void SendUserLoc()
	{
		
		String line = "User_Loc" + "," + UserLat + "," + UserLong;
		pw.println(line);
		pw.flush();
		//System.out.println("Thread " + threadNum + " sending User Location: " + line);
	}
	
	private void SendUserStartTime()
	{
		
		String line = "User_Time" + "," + System.currentTimeMillis();
		pw.println(line);
		pw.flush();
		//System.out.println("Thread " + threadNum + " sending User Time: " + line);
	}
	
	public void SendUserDone()
	{
		String line = "All orders completed!";
		pw.println(line);
		pw.flush();
		//System.out.println("Thread " + threadNum + " sending User Done: " + line);
		
		programEnd = true;
	}
	
	public static void SetUserLoc(Double uLat, Double uLong)
	{
		UserLat = uLat;
		UserLong = uLong;
	}
	
	public void DeliverOrders(Vector<Order> orders)
	{
		available = false;
		//System.out.println("Thread " + threadNum + "Getting orders!");
		
		myOrders = orders;
		
		String csv = "";
		for(int i = 0; i < myOrders.size(); i++)
		{
			//System.out.println("Trying to fetch " + myOrders.get(i).getRestaurant());
			Restaurant rest = server.GetRestaurants().fetchRestaurant(myOrders.get(i).getRestaurant());
			csv += rest.getName() + "," + myOrders.get(i).getMenuItem() + "," + rest.getLatitude() + "," + rest.getLongitude();
			if(i < myOrders.size() - 1)
			{
				csv += ",";
			}
		}
		myOrders.clear();
		
		//System.out.println("Thread " + threadNum + " broadcasting info: " + csv);
		
		pw.println(csv);
		pw.flush();
	
	}
	
	//END OF DELIVER ORDERS
	
	public void run()
	{
		//System.out.println("Starting Server Thread " + threadNum + " run!");
		SendEnoughDrivers();
		while(!programEnd)
		{
			//keep listening for client message
			if(programEnd)
			{
				available = true;
				break;
			}
				
			//System.out.println("Thread " + threadNum + " listening for Driver messages!");
			String messageRecieved = "No message recieved";
			
			try {
				messageRecieved = br.readLine();
			} 
			catch (IOException e) {
				e.printStackTrace();
			} 
			
			if(programEnd)
			{
				available = true;
				break;
			}
			
			//System.out.println("Thread " + threadNum + " available again!");
			available = true;
				
			//System.out.println("Thread " + threadNum + " after deliveries, message: " + messageRecieved);
			
			
		}	
	}
	
	public boolean getAvailable()
	{
		return available;
	}
		
	
	
	
	
	
}
