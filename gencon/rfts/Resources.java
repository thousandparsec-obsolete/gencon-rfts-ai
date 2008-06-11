package gencon.rfts;


/**
 * A container for the resources available on {@link Planet}s in RFTS.
 *
 * @author Victor Ivri
 */
public class Resources 
{
	public static enum ResourceType
	{
		RESOURCE_POINT, INDUSTRY, POPULATION, SOCIAL_ENV, PLANETARY_ENV, POP_MAINTANANCE;
	}
	
	private int resource_points;
	private int industry;
	private int population;
	private int social_env;
	private int planetary_env;
	private int pop_maintanance;
	
	/**
	 *	Default constructor; all resources initialized to 0;
	 */
	public Resources()
	{
		resource_points = 0; industry = 0; population = 0; 
		social_env = 0; planetary_env = 0; pop_maintanance = 0;
	}
	
	
	/**
	 * Deep copy constructor.
	 * @param other Another set of resources.
	 */
	public Resources(Resources other)
	{
		industry = other.getResource(ResourceType.INDUSTRY);
		planetary_env = other.getResource(ResourceType.PLANETARY_ENV);
		pop_maintanance = other.getResource(ResourceType.POP_MAINTANANCE);
		population = other.getResource(ResourceType.POPULATION);
		resource_points = other.getResource(ResourceType.PLANETARY_ENV);
		social_env = other.getResource(ResourceType.SOCIAL_ENV);
	}
	
	
	/**
	 * @param type One of {@link Resources.ResourceType}.
	 * @return The amount of that resource.
	 */
	public int getResource(Resources.ResourceType type)
	{
		switch(type)
		{
			case RESOURCE_POINT: return resource_points;
			case INDUSTRY: return industry;
			case POPULATION: return population;
			case SOCIAL_ENV: return social_env;
			case PLANETARY_ENV: return planetary_env;
			case POP_MAINTANANCE: return pop_maintanance;
			default: return -1; //should never be default.
		}
	}
	
	/**
	 * Sets the specified resource to a new quantity.
	 * @param type One of {@link Resources.ResourceType}.
	 * @param amount The quantity to be set.
	 */
	public void setResource(Resources.ResourceType type, int quantity)
	{
		switch(type)
		{
			case RESOURCE_POINT: resource_points = quantity;
			case INDUSTRY: industry = quantity;
			case POPULATION: population = quantity;
			case SOCIAL_ENV: social_env = quantity;
			case PLANETARY_ENV: planetary_env = quantity;
			case POP_MAINTANANCE: pop_maintanance = quantity;
		}
	}
	
}
