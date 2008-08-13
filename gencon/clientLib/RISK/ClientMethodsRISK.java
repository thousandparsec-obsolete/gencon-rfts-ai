package gencon.clientLib.RISK;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.ObjectParams;
import net.thousandparsec.util.Pair;

import gencon.clientLib.Client;
import gencon.clientLib.ClientMethods;
import gencon.gamelib.RISK.gameobjects.RiskGameObject;

public class ClientMethodsRISK extends ClientMethods
{
	public ClientMethodsRISK(Client client)
	{
		super(client);
	}

	public synchronized Collection<RiskGameObject> getAllRiskObjects() throws TPException, IOException
	{
		Collection<Object> game_objects = CLIENT.getClientMethods().getAllObjects();
		
		return convertAllObjects(game_objects);
	}
	
	public synchronized Collection<RiskGameObject> convertAllObjects(Collection<Object> objects)
	{
		//arrange all objects by category:
		Collection<Object> galaxies = new HashSet<Object>();
		Collection<Object> wormholes = new HashSet<Object>();
		Collection<Object> starSystems = new HashSet<Object>();
		Collection<Object> planets = new HashSet<Object>();
		
		for (Object object : objects)
		{
			if (object.getObject().getParameterType() == ObjectParams.Galaxy.PARAM_TYPE
					&& !object.getName().equals("Wormholes"))
				galaxies.add(object);
			else if (object.getObject().getParameterType() == ObjectParams.Wormhole.PARAM_TYPE)
				wormholes.add(object);
			else if (object.getObject().getParameterType() == ObjectParams.StarSystem.PARAM_TYPE)
				starSystems.add(object);
			else if (object.getObject().getParameterType() == ObjectParams.Planet.PARAM_TYPE)
				planets.add(object);
		}
		
		//preparing cotainer of converted bodies
		Collection<RiskGameObject> riskObjects = new HashSet<RiskGameObject>(objects.size());
		
		//converting galaxies and wormholes, and adding to the container:
		for (Object galaxy : galaxies)
			riskObjects.add(ObjectConverterRISK.convertConstellation(galaxy));
		for (Object wormhole : wormholes)
			riskObjects.add(ObjectConverterRISK.convertWormhole(wormhole));
		
		//pairing up star systems with planets:
		Collection<Pair<Object, Object>> Star_pairs = new HashSet<Pair<Object,Object>>();
		for (Object ss : starSystems)
		{
			int idContains = ss.getContains().get(0).getId();
			
			for (Object pl : planets)
				if (pl.getId() == idContains)
				{
					Pair<Object, Object> star_pair = new Pair<Object, Object>(ss, pl); 
					Star_pairs.add(star_pair); 
					break;
				}
		}
		assert starSystems.size() == planets.size(); //to make sure they are paired up correspondingly!
		
		//converting the star-pairs, and adding to the container:
		for (Pair<Object, Object> pair : Star_pairs)
			riskObjects.add(ObjectConverterRISK.convertStar(pair.left, pair.right));
		
		
		//if this point is reached, then everything's fine.
		return riskObjects;
	}
}
