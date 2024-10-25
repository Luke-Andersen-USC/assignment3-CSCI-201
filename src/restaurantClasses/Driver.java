package restaurantClasses;

import java.io.BufferedReader;
import java.io.File;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Driver {

	private static Scanner scanner;
	private static Vector<Order> orders;
	private static Double userLat = 0.0;
	private static Double userLong = 0.0;
	
	private static Lock lock = new ReentrantLock();
	public static Condition enoughDrivers = lock.newCondition();
	
	private static int driversReq = 0;
	private static int driversCur = 0;
	private static int portNum = 3456;
	
	private ServerSocket ss = null;
	private static Socket s = null;
	
	public static void main(String []args) throws NumberFormatException, IOException
	{
		scanner = new Scanner(System.in);
		
		System.out.println("Welcome to JoesTable v2.0!");
		
		System.out.println("Enter the server hostname:");
		
		String hostName = scanner.nextLine();
		
		System.out.println("Enter the server port:");
		
		int port = Integer.parseInt(scanner.nextLine());
		
		try {
			s = new Socket(hostName, port);
		} 
		catch (UnknownHostException e) {
			
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		scanner.close();		
		
		//END OF USER INPUT - START OF METHOD
		
	}
	
		



	
}
