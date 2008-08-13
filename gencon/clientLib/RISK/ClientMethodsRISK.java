package gencon.clientLib.RISK;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.Object;

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
	
	private synchronized Collection<RiskGameObject> convertAllObjects(Collection<Object> objects)
	{
		Collection<RiskGameObject> riskObjects = new HashSet<RiskGameObject>(objects.size());
		
		for (Object object : objects)
			riskObjects.add(ObjectConverterRISK.convertObject(object));
		
		return riskObjects;
	}

}
