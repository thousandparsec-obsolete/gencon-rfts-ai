package gencon;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

public class Runner {

	/**
	 * The runner class of the AI client
	 * @param args
	 */
	public static void main(String[] args) 
	{
		System.out.println("Genetic Conquest: An AI Client for Thousand Parsec : RFTS ruleset.");
		
		Scanner stin = new Scanner(System.in);
		System.out.print("Please provide a URI string to establish connection with server: ");
		String URIString = stin.next();
		
/*		~~A BIT MORE USER FRIENDLY, BUT UNNECESSARY IN THIS STAGE
 * 		System.out.print("Enter the address of the server : ");
		String server = stin.next();
		System.out.print("Enter your username : ");
		String usrname = stin.next();
		System.out.print("Enter your password : ");
		String pwd = stin.next();
		
		 //WORKING ASSUMPTION : 
		 //the usr+address string is formatted thus:   
		 //~assumption taken from the formatting in TestConnect.java~ 
		String URIString = "tp://" + pwd + ":" + usrname + "@" + server;
*/
		
		
		//starting up the client
		Client client = new Client();
		client.init(URIString);

		client.run();
		
	}
	
}
