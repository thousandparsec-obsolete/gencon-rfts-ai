package gencon.gamelib.gameobjects;

public abstract class Order 
{
	public final int ORDER_TYPE;
	public final int OBJECT_ID;
	public final int PLACE_IN_QUEUE;
	
	public Order(int order_type, int object_id, int place_in_queue)
	{
		ORDER_TYPE = order_type;
		OBJECT_ID = object_id;
		PLACE_IN_QUEUE = place_in_queue;
	}
	
}
