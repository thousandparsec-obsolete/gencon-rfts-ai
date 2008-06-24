package gencon.gamelib.gameobjects;

public abstract class GenericOrder 
{
	public final int ORDER_TYPE_ID;
	public final int OBJECT_ID;
	public final int PLACE_IN_QUEUE;
	
	public GenericOrder(int order_type_id, int object_id, int place_in_queue)
	{
		ORDER_TYPE_ID = order_type_id;
		OBJECT_ID = object_id;
		PLACE_IN_QUEUE = place_in_queue;
	}
	
}
