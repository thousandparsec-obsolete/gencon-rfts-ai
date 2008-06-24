package gencon.gamelib.gameobjects;

/**
 * Stores the resources available on {@link Planet}s in RFTS.
 *
 * @author Victor Ivri
 */
public class Resources 
{
	public final int RESOURCE_POINTS;
	public final int INDUSTRY;
	public final int POPULATION;
	public final int SOCIAL_ENV;
	public final int PLANETARY_ENV;
	public final int POP_MAINTANENCE;
	public final int COLONIST;
	public final int SHIP_TECH;
	public final int PDB1S;
	public final int PDB1_MAINT;
	public final int PDB2S;
	public final int PDB2_MAINT;
	public final int PDB3S;
	public final int PDB3_MAINT;
	
	public Resources(int resource_pts, int industry, int population, int social_env,
			int planetary_env, int pop_maintanance, int colonist, int ship_tech, int pdb1, int pdb1_m,
			int pdb2, int pdb2_m, int pdb3, int pdb3_m)
	{
		RESOURCE_POINTS = resource_pts;
		INDUSTRY = industry;
		POPULATION = population;
		SOCIAL_ENV = social_env;
		PLANETARY_ENV = planetary_env;
		POP_MAINTANENCE = pop_maintanance;
		COLONIST = colonist;
		SHIP_TECH = ship_tech;
		PDB1S = pdb1;
		PDB1_MAINT = pdb1_m;
		PDB2S = pdb2;
		PDB2_MAINT = pdb2_m;
		PDB3S = pdb3;
		PDB3_MAINT = pdb3_m;
	}
}
