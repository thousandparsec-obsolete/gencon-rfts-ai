package gencon.robolib.RISK;

import gencon.evolutionlib.Genotype.Alleles;
import gencon.gamelib.RISK.UniverseMap;

import java.util.Map;

public class ActionController 
{
	private final ActionMethods ACTIONS;
	private int myPlayerId;
	
	
	public ActionController(ActionMethods actions)
	{
		ACTIONS = actions;
	}
	
	public void incrementTurn(UniverseMap newMap, int myPlrId)
	{
		myPlayerId = myPlrId;
		ACTIONS.incrementTurn(newMap, myPlrId);
	}
	
	
	public void performActions(Map<Alleles, Byte> currentTraits)
	{
		//assign vaules to genes:
		byte geneBackwaterDistribute = currentTraits.get(Alleles.ALLELE_1);
		byte geneDefence = currentTraits.get(Alleles.ALLELE_2);
		byte geneReinforce = currentTraits.get(Alleles.ALLELE_3);
		byte geneBravery = currentTraits.get(Alleles.ALLELE_4);
		byte geneCannonfodder = currentTraits.get(Alleles.ALLELE_5);
		byte geneExpansionism = currentTraits.get(Alleles.ALLELE_6);
		byte geneEmigration = currentTraits.get(Alleles.ALLELE_7);
		byte geneStoicism = currentTraits.get(Alleles.ALLELE_8);
		byte geneAggression = currentTraits.get(Alleles.ALLELE_9);
		byte geneCheapness = currentTraits.get(Alleles.ALLELE_10);
		
		//------------------------------------
		//LET'S FIRE 'ER UP. DIE, INFIDELS!
		
		//always first:
		ACTIONS.transferTroopsFromBackwaterStars(geneBackwaterDistribute, myPlayerId);
		ACTIONS.reinforceEndangeredPlanets(geneDefence, geneReinforce, myPlayerId);
		ACTIONS.distributeRemainingReinforcements(geneCheapness, myPlayerId);
		
		//depends on aggression:
		if (geneAggression == 0) //expand first, attack later.
		{
			ACTIONS.expandToNeutralStars(geneExpansionism, geneEmigration, myPlayerId);
			ACTIONS.offensiveActions(geneBravery, geneCannonfodder, myPlayerId);
		}
		else if (geneAggression == 1) //equal chance to be first.
		{
			if (Math.random() < 0.5)
			{
				ACTIONS.expandToNeutralStars(geneExpansionism, geneEmigration, myPlayerId);
				ACTIONS.offensiveActions(geneBravery, geneCannonfodder, myPlayerId);
			}
			else
			{
				ACTIONS.offensiveActions(geneBravery, geneCannonfodder, myPlayerId);
				ACTIONS.expandToNeutralStars(geneExpansionism, geneEmigration, myPlayerId);
			}
		}
		else //attack first!
		{
			ACTIONS.offensiveActions(geneBravery, geneCannonfodder, myPlayerId);
			ACTIONS.expandToNeutralStars(geneExpansionism, geneEmigration, myPlayerId);
		}
		
		//always last:
		ACTIONS.evacuateToSafety(geneStoicism, myPlayerId);
	}
	
	
	
}
