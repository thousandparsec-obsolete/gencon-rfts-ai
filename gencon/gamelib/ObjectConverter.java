package gencon.gamelib;


import gencon.clientLib.Client;
import gencon.clientLib.ConnectionMethods;
import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.Galaxy;
import gencon.gamelib.gameobjects.Planet;
import gencon.gamelib.gameobjects.StarSystem;
import gencon.gamelib.gameobjects.Universe;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.tp03.ObjectParams;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.Object.PosType;
import net.thousandparsec.netlib.tp03.Object.ContainsType;
import net.thousandparsec.netlib.tp03.Object.VelType;
import net.thousandparsec.netlib.tp03.ObjectParams.Fleet.ShipsType;
import net.thousandparsec.netlib.tp03.Object;

import gencon.utils.*;

/**
 * A static class which converts classes from protocol library, to classes from gencon.gamelib library.
 * 
 * @author Victor Ivri
 *
 */
public class ObjectConverter 
{
	private ObjectConverter(){} //dummy constructor.
	
	
	public static synchronized Body ConvertToBody(Object object, int parent, Client client, Players currentPlayers)
	{
		//Generic parameters:
		//-----------------------
		int game_id = object.getId();
		String name = object.getName();
			//3D position:
		PosType pt = object.getPos();
		long[] position = {pt.getX(), pt.getY(), pt.getZ()};
		
		long modtime = object.getModtime();
		
			//3D velocity:
		VelType vt = object.getVel();
		long[] velocity = {vt.getX(), vt.getY(), vt.getZ()};
		
			//setting children:
		List<ContainsType> contains = object.getContains();
		List<Integer> children = new ArrayList<Integer>();
		for (ContainsType ct : contains)
			if (ct != null)
				children.add(new Integer(ct.getId()));
		
		//---------------------------------
		//Type-specific params and Body instantiation: (Depending on the type of the object)
		
		int object_type = object.getObject().getParameterType();
		
		switch (object_type)
		{
			case (ObjectParams.Universe.PARAM_TYPE): 
				return new Universe(game_id, modtime, name, children);
			case (ObjectParams.Galaxy.PARAM_TYPE):
				return new Galaxy(game_id, modtime, name, position, parent, children);

			case (ObjectParams.StarSystem.PARAM_TYPE):
				return new StarSystem(game_id, modtime, name, position, parent, children);
				
			case (ObjectParams.Planet.PARAM_TYPE):
			{
				//getting owner:
				ObjectParams.Planet pl = (ObjectParams.Planet) object.getObject();
				Game_Player owner = new Game_Player(pl.getOwner(), client.getPlayerById(pl.getOwner()).getName());
				
				//getting orders:
				
				//getting resources:
				
				//getting pdbs:
				
				
				return new Planet(game_id, modtime, name, position, parent, children, owner, orders, resources, pdbs);
			}
			case (ObjectParams.Fleet.PARAM_TYPE):
			{
				//getting owner:
				ObjectParams.Fleet fl = (ObjectParams.Fleet) object.getObject();
				Game_Player owner = new Game_Player(fl.getOwner(), client.getPlayerById(fl.getOwner()).getName());
				
				//getting damage:
				int damage = fl.getDamage();
				
				//getting ships:
				List<ShipsType> shipList = fl.getShips();
				//........ GOTTA LEARN THE ACTUAL STRING NAMES, AND CONVERT THEM TO TYPES...
				
				//getting velocity:
				VelType vel = object.getVel();
				long[] speed = {vel.getX(), vel.getY(), vel.getZ()};
				
				//getting orders:
				
				
				return new Fleet(game_id, modtime, name, position, owner, parent, children, damage, ships, orders, speed);
				
			}
			
			default: return null; //making compiler happy...
		}
	}

	
	public static synchronized gencon.gamelib.gameobjects.Order convertOrder(net.thousandparsec.netlib.tp03.Order order)
	{
		
	}
	
	public static synchronized gencon.gamelib.gameobjects.Planet.Resources convertResources(Vector<net.thousandparsec.netlib.tp03.Resource> resources)
	{
		
	}
	
	public static synchronized gencon.gamelib.gameobjects.Fleet.Ship convertShip(net.thousandparsec.netlib.tp03.ObjectParams.Fleet.ShipsType ship)
	{
		
	}
}
