package gencon.gamelib;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.GetObjectsByID;
import net.thousandparsec.netlib.tp03.GetWithID;
import net.thousandparsec.netlib.tp03.Sequence;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.GetWithID.IdsType;
import net.thousandparsec.netlib.tp03.Object;

public class UniverseTree
{
	public final Body ROOT;
	
	/**
	 * @param root Cannot be null!
	 */
	public UniverseTree(Body root)
	{
		ROOT = root;
	}
	
	
	public void createTree(SequentialConnection<TP03Visitor> conn)  throws TPException, IOException
	{
		Vector<Body> roots = new Vector<Body>();
		roots.add(ROOT);
		populateBreadthFirst(roots, conn);
	}
	
	private void populateBreadthFirst(Vector<Body> roots, SequentialConnection<TP03Visitor> conn) throws TPException, IOException
	{
		//BASE CASE: BOTTOM-LEVEL REACHED
		if (roots == null || roots.isEmpty()) 
		{
			return;
		}
		
		//RECURSIVE CASE: REACHING DOWN ONE LEVEL
		else 
		{
			//preparing for parent-child mapping.
			Map<Integer, Body> parent_child = new HashMap<Integer, Body>(); //find parent by child
		
			//preparing the frame for sending:
			GetWithID getObjects = new GetObjectsByID();
			
			//iterating over the current tree-level of bodies:
			for (Body parent : roots)
				if (parent != null)
				{
					//getting the children:
					List<Integer> children = parent.CHILDREN;
					
					//populating the frame with ids to be retreived, and storing children in the parent-child map:
					for (Integer child : children)
						if (child != null)
						{
							parent_child.put(child, parent);
							getObjects.getIds().add(new IdsType(child.intValue()));
						}
				}
			
			//getting the sequence frame:
			Sequence seq = conn.sendFrame(getObjects, Sequence.class); //THROWS TP AND IO EXCEPTIONS!!! IF THIS OPERATION FAILS, NO POINT IN CONTINUING.
			//retreiving the Object frames:
			Vector<Object> nextLevel = new Vector<Object>();
			for (int i = 0; i < seq.getNumber(); i++)
			{
				try
				{
					Object obj = conn.receiveFrame(Object.class);
					nextLevel.add(obj);
				}
				catch (Exception ignore){}
			}
			
			//iterating over the objects, to match them with a parent, and convert them to Bodies.
			Vector<Body> newRoots = new Vector<Body>();
			for (Object obj : nextLevel)
				if (obj != null)
				{
					Body parent = parent_child.get(obj.getId()); //should be there! otherwise, something wrong went on!!!
						if (parent == null)
							throw new TPException("Parent not found");
					Body child = ObjectConverter.ConvertToBody(obj, parent.GAME_ID, conn);
					newRoots.add(child);
				}
			
			//recurse over the next level in the body-tree!!! (sounds like bodhi tree)
			populateBreadthFirst(newRoots, conn);
		}
	}
}
