package gencon.clientLib;


import gencon.clientLib.Client;
import gencon.clientLib.ConnectionMethods;
import gencon.gamelib.Game_Player;
import gencon.gamelib.Players;
import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.Galaxy;
import gencon.gamelib.gameobjects.GenericOrder;
import gencon.gamelib.gameobjects.Planet;
import gencon.gamelib.gameobjects.Ships;
import gencon.gamelib.gameobjects.StarSystem;
import gencon.gamelib.gameobjects.Universe;
import gencon.gamelib.gameobjects.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.tp03.ObjectParams;
import net.thousandparsec.netlib.tp03.ResourceIDs;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.Object.OrdertypesType;
import net.thousandparsec.netlib.tp03.Object.PosType;
import net.thousandparsec.netlib.tp03.Object.ContainsType;
import net.thousandparsec.netlib.tp03.Object.VelType;
import net.thousandparsec.netlib.tp03.ObjectParams.Fleet.ShipsType;
import net.thousandparsec.netlib.tp03.ObjectParams.Planet.ResourcesType;
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
	
	
	public static synchronized Body ConvertToBody(Object object, int parent, Client client)
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
				Game_Player owner = client.getPlayerById(pl.getOwner());
				
				//getting orders:
				
				//getting resources:
				Resources resources = convertResources(pl.getResources());
				
				return new Planet(game_id, modtime, name, position, parent, children, owner, orders, resources);
			}
			case (ObjectParams.Fleet.PARAM_TYPE):
			{
				//getting owner:
				ObjectParams.Fleet fl = (ObjectParams.Fleet) object.getObject();
				Game_Player owner = client.getPlayerById(fl.getOwner());
				
				//getting damage:
				int damage = fl.getDamage();
				
				//getting ships:
				Ships ships = convertShip(fl);
				
				
				//getting velocity:
				VelType vel = object.getVel();
				long[] speed = {vel.getX(), vel.getY(), vel.getZ()};
				
				//getting orders:
				
				
				return new Fleet(game_id, modtime, name, position, owner, parent, children, damage, ships, orders, speed);
				
			}
			
			default: return null; //making compiler happy...
		}
	}

	
	public static synchronized GenericOrder convertOrder(net.thousandparsec.netlib.tp03.Order order)
	{
		
	}
	
	public static synchronized Resources convertResources(List<ResourcesType> resources)
	{
		//initializing variables to nill:
		int resource_pts = 0; int industry = 0; int population = 0;
		int social_env = 0; int planetary_env = 0; int pop_maintanance = 0; 
		int colonist = 0; int ship_tech = 0; int pdb1 = 0; int pdb1_m = 0;
		int pdb2 = 0; int pdb2_m = 0; int pdb3 = 0; int pdb3_m = 0;
		
		// retreiving data:
		for (ResourcesType rt : resources)  
			if (rt != null)
			{
				switch (rt.getId())  //rfts-specific mapping.
				{
					case (1): resource_pts = rt.getUnits();
					case (2): industry = rt.getUnits();
					case (3): population = rt.getUnits();
					case (4): social_env = rt.getUnits();
					case (5): planetary_env = rt.getUnits();
					case (6): pop_maintanance = rt.getUnits();
					case (7): colonist = rt.getUnits();
					case (8): ship_tech = rt.getUnits();
					case (9): pdb1 = rt.getUnits();
					case (10): pdb1_m = rt.getUnits();
					case (11): pdb2 = rt.getUnits();
					case (12): pdb2_m = rt.getUnits();
					case (13): pdb3 = rt.getUnits();
					case (14): pdb3_m = rt.getUnits();
				}
			}
		
		return new Resources(resource_pts, industry, population, social_env, planetary_env,
				pop_maintanance, colonist, ship_tech, pdb1, pdb1_m, pdb2, pdb2_m, pdb3, pdb3_m);
	}
	
	public static synchronized Ships convertShip(ObjectParams.Fleet fleet)
	{
		
	}
}
