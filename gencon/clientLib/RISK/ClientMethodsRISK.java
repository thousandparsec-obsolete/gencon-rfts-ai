package gencon.clientLib.RISK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.ObjectParams;
import net.thousandparsec.netlib.tp03.OrderInsert;
import net.thousandparsec.netlib.tp03.OrderParams;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.OrderParams.OrderParamList.SelectionType;
import net.thousandparsec.util.Pair;

import gencon.clientLib.Client;
import gencon.clientLib.ClientMethods;
import gencon.clientLib.ConnectionMethods;
import gencon.gamelib.RISK.gameobjects.OrderTypes;
import gencon.gamelib.RISK.gameobjects.RiskGameObject;
import gencon.gamelib.RISK.gameobjects.Star;

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
	
	
	public synchronized boolean orderMove(Star from, Star to, int troops, boolean urgent) throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		
		OrderInsert order = new OrderInsert();
		order.setOtype(OrderTypes.MOVE);
		order.setId(from.GAME_ID);
		
		if (!urgent)
			order.setSlot(-1); //sets the location of the order at the end of the queue.
		else
			order.setSlot(0); //sets the location of the order at the beginning of the queue.
		
		//setting the parameters:
		OrderParams.OrderParamList list = new OrderParams.OrderParamList();
		
		//setting destination and troops:
		SelectionType st = new SelectionType();
		st.setId(to.GAME_ID);
		st.setNumber(troops);
		list.getSelection().add(st);
		
		try
		{
		//registering the parameters:
			List<OrderParams> op = new ArrayList<OrderParams>(1);
			op.add(list);
			order.setOrderparams(op, ConnectionMethods.getODbyId(order.getOtype(), conn)); 

		//getting result:
			return ConnectionMethods.sendOrder(order, conn);
		}
		finally
		{
			conn.close();
		}
	}
	
	public synchronized boolean orderColonize(Star star, int troops, boolean urgent) throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		
		OrderInsert order = new OrderInsert();
		order.setOtype(OrderTypes.COLONIZE);
		//order.setId(star.PLANET_ID);
		
		
		if (!urgent)
			order.setSlot(-1); //sets the location of the order at the end of the queue.
		else
			order.setSlot(0); //sets the location of the order at the beginning of the queue.
		
		//setting the parameters:
		OrderParams.OrderParamList list = new OrderParams.OrderParamList();
		
		//setting destination and troops:
		SelectionType st = new SelectionType();
		st.setId(star.GAME_ID);
		st.setNumber(troops);
		list.getSelection().add(st);
		
		try
		{
		//registering the parameters:
			List<OrderParams> op = new ArrayList<OrderParams>(1);
			op.add(list);
			order.setOrderparams(op, ConnectionMethods.getODbyId(order.getOtype(), conn)); 

		//getting result:
			return ConnectionMethods.sendOrder(order, conn);
		}
		finally
		{
			conn.close();
		}
	}
	
	public synchronized boolean orderReinforce(Star star, int troops, boolean urgent) throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		
		OrderInsert order = new OrderInsert();
		order.setOtype(OrderTypes.REINFORCE);
		order.setId(star.GAME_ID);
		
		if (!urgent)
			order.setSlot(-1); //sets the location of the order at the end of the queue.
		else
			order.setSlot(0); //sets the location of the order at the beginning of the queue.
		
		//setting the parameters:
		OrderParams.OrderParamTime param = new OrderParams.OrderParamTime();
		param.setTurns(troops);
		
		try
		{
		//registering the parameters:
			List<OrderParams> op = new ArrayList<OrderParams>(1);
			op.add(param);
			order.setOrderparams(op, ConnectionMethods.getODbyId(order.getOtype(), conn)); 

		//getting result:
			return ConnectionMethods.sendOrder(order, conn);
		}
		finally
		{
			conn.close();
		}
	}
}
