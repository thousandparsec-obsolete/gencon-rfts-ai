package gencon.clientLib.RFTS;

import gencon.clientLib.Client;
import gencon.gamelib.Game_Player;
import gencon.gamelib.Players.Game_Player;
import gencon.gamelib.RFTS.gameobjects.Body;
import gencon.gamelib.RFTS.gameobjects.Fleet;
import gencon.gamelib.RFTS.gameobjects.Galaxy;
import gencon.gamelib.RFTS.gameobjects.Planet;
import gencon.gamelib.RFTS.gameobjects.Resources;
import gencon.gamelib.RFTS.gameobjects.Ships;
import gencon.gamelib.RFTS.gameobjects.StarSystem;
import gencon.gamelib.RFTS.gameobjects.Universe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp04.ObjectDesc;
import net.thousandparsec.netlib.tp04.ObjectParams;
import net.thousandparsec.netlib.tp04.Player;
import net.thousandparsec.netlib.tp04.TP04Visitor;
import net.thousandparsec.netlib.tp04.GetObjectIDsByPos.PosType;
import net.thousandparsec.netlib.tp04.Object.ContainsType;
import net.thousandparsec.netlib.tp04.ObjectParams.ObjectParamPosition3d;
import net.thousandparsec.netlib.tp04.ObjectParams.ObjectParamPosition3d.PositionType;
import net.thousandparsec.netlib.tp04.ObjectParams.ObjectParamResourceList.ResourcesType;
import net.thousandparsec.netlib.tp04.ObjectParams.ObjectParamVelocity3d.VelocityType;
import net.thousandparsec.netlib.tp04.Object;


//////
//////	THIS CLASS NEEDS TO CONFORM TO TP04!!!
//////
//////



/**
 * A static class which converts classes from protocol library, to classes from gencon.gamelibRFTS library.
 * 
 * @author Victor Ivri
 *
 */
public class ObjectConverter 
{
	private ObjectConverter(){} //dummy constructor.
	
	
	public static synchronized Body convertToBody(Object object, int parent, Client client, ObjectDesc od) throws IOException, TPException
	{
		//Generic parameters:
		//-----------------------
		List<ObjectParams> params = object.getParameters(od);
		
		
		int game_id = object.getId();
		String name = object.getName();
			//3D position:
		
		//getting the correct param:
		ObjectParams.ObjectParamPosition3d pos = null;
		for (ObjectParams op : params)
			if (op.getParameterType() == ObjectParamPosition3d.PARAM_TYPE)
				pos = (ObjectParamPosition3d)op;
		
		assert pos != null; //there must be a position!
		
		PositionType pt = pos.getPosition();
		long[] position = {pt.getX(), pt.getY(), pt.getZ()};
		
		long modtime = object.getModtime();
		
			//setting children:
		List<ContainsType> contains = object.getContains();
		List<Integer> children = new ArrayList<Integer>();
		for (ContainsType ct : contains)
			if (ct != null)
				children.add(new Integer(ct.getId()));
		
//		getting number of orders on object:
		int orders = object.getOrders();
		
		//---------------------------------
		//Type-specific params and Body instantiation: (Depending on the type of the object)
		
		int object_type = object.getOtype();
		
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
				int owner = pl.getOwner(); 
				
				//getting resources:
				Resources resources = convertResources(pl.getResources());
				
				return new Planet(game_id, modtime, name, position, parent, children, owner, orders, resources);
			}
			case (ObjectParams.Fleet.PARAM_TYPE):
			{
				//getting owner:
				ObjectParams.Fleet fl = (ObjectParams.Fleet) object.getObject();
				int owner = fl.getOwner();
				
				//getting damage:
				int damage = fl.getDamage();
				
				//getting ships:
				Ships ships = convertShips(fl);
				
				//getting velocity:
				VelType vel = object.getVel();
				long[] speed = {vel.getX(), vel.getY(), vel.getZ()};
				
				return new Fleet(game_id, modtime, name, position, owner, parent, children, damage, ships, orders, speed);
				
			}
			
			default: return null; //making compiler happy...
		}
	}
	
	private static synchronized Resources convertResources(List<ResourcesType> resources)
	{
		//initializing variables to nill:
		int resource_pts = 0, industry = 0, population = 0;
		int social_env = 0, planetary_env = 0, pop_maintanance = 0; 
		int colonist = 0, ship_tech = 0, pdb1 = 0, pdb1_m = 0;
		int pdb2 = 0, pdb2_m = 0, pdb3 = 0, pdb3_m = 0;
		
		// retreiving data:
		for (ResourcesType rt : resources)  
			if (rt != null)
			{
				switch (rt.getResourceid())  //rfts-specific mapping.
				{
					case (1): resource_pts = rt.getStored();
					case (2): industry = rt.getStored();
					case (3): population = rt.getStored();
					case (4): social_env = rt.getStored();
					case (5): planetary_env = rt.getStored();
					case (6): pop_maintanance = rt.getStored();
					case (7): colonist = rt.getStored();
					case (8): ship_tech = rt.getStored();
					case (9): pdb1 = rt.getStored();
					case (10): pdb1_m = rt.getStored();
					case (11): pdb2 = rt.getStored();
					case (12): pdb2_m = rt.getStored();
					case (13): pdb3 = rt.getStored();
					case (14): pdb3_m = rt.getStored();
				}
			}
		
		return new Resources(resource_pts, industry, population, social_env, planetary_env,
				pop_maintanance, colonist, ship_tech, pdb1, pdb1_m, pdb2, pdb2_m, pdb3, pdb3_m);
	}
	
	/*
	 * Note that FOR NOW, it does not seek out designs by name, and dynamically assigns them, 
	 * but uses a static mapping between ships-types and designs, which works for now.
	 */
	private static synchronized Ships convertShips(ObjectParams fleet)
	{
		List<ShipsType> shipTypes = fleet.getShips();
		
		int scouts = 0, Mk1 = 0, transports = 0, Mk2 = 0, Mk3 = 0, Mk4 = 0;
		
		//extract all the ships:
		for (ShipsType st : shipTypes)
		{
			switch (st.getType())
			{
				case (1): scouts = st.getCount();
				case (2): Mk1 = st.getCount();
				case (3): transports = st.getCount();
				case (4): Mk2 = st.getCount();
				case (5): Mk3 = st.getCount();
				case (6): Mk4 = st.getCount();
			}
		}
		
		return new Ships(transports, scouts, Mk1, Mk2, Mk3, Mk4);
	}
	
	
	public synchronized static Game_Player convertPlayer(Player player)
	{
		return new Game_Player(player.getId(), player.getName());
	}
}
