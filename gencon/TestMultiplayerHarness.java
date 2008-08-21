package gencon;

public class TestMultiplayerHarness 
{
	public static void main(String[] args)
	{
		String[] args1 = new String[args.length];
		String[] args2 = new String[args.length];
		
		for (int i = 0; i < args.length; i++)
		{
			args1[i] = new String(args[i]);
			args2[i] = new String(args[i]);
		}
		
		//changing the uri: (username)
		args1[1] = "tp://robot1:a@localhost";
		args1[1] = "tp://robot2:a@localhost";
		
		Master master1 = new Master(args1, System.out);
		Master master2 = new Master(args2, System.out);
		
		Thread game1 = new Thread(master1);
		Thread game2 = new Thread(master2);
		
		game1.run();
		game2.run();
	}
}
