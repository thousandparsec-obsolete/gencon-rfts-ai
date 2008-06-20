package gencon.gamelib;

import gencon.gamelib.gameobjects.Body;

import java.util.Vector;


/**
 * A 3D representation of the game-world. Ids of objects are stored, instead of objects themselves.
 * 
 * @author Victor Ivri
 */
public class UniverseMap 
{
	public final Vector<Body> BODIES;
	
	public UniverseMap(Vector<Body> bodies)
	{
		BODIES = bodies;
	}
	
	//BODY-RETREIVAL METHODS: (E.G. GET N-CLOSEST BODIES, ETC)
}
