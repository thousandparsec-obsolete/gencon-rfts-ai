package gencon.clientLib;

import gencon.gamelib.AbstractGameObject;

import java.io.IOException;
import java.util.List;

import net.thousandparsec.netlib.TPException;

public abstract class ClientMethods 
{
	public final Client CLIENT;
	
	public ClientMethods(Client client)
	{
		CLIENT = client;
	}
	
}
