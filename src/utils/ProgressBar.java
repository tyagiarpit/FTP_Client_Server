package utils;

public class ProgressBar {
	private static final String INCOMPLETE = ".";
	private static final String COMPLETE = "=";
	private static final int CONSOLE_WIDTH = 80;
	private static final boolean ENABLED = true;
	private static int pct = 0;
	private static double pos = 0;
	private static int bar = 0;
	private static double _total = 0;
	public static void init(int total){
		_total = total;
		if(!ENABLED)
			return;
		System.out.print("\n - ");//4
		System.out.print("[");//1
		bar = CONSOLE_WIDTH-12;
		for(int i=0;i<bar;i++)
			System.out.print(INCOMPLETE);//1
		System.out.print("]");//1
		System.out.printf(" %3d",1);//4
		System.out.print("% ");
	}
	
	public static void tick(int _current){
		double tempPct = Math.ceil(((double)_current*100)/_total);
		if(tempPct>pct)
			pct = (int) tempPct;
		pos = Math.ceil(((double)bar)*((double)pct)/100);
		for(int i = 0;i<CONSOLE_WIDTH;i++ )
			System.out.print("\b");
		if(pct==100)
			System.out.print(" * ");//4
		else if(pct%4==0)
			System.out.print(" - ");//4
		else if(pct%4==1)
			System.out.print(" / ");//4
		else if(pct%4==2)
			System.out.print(" - ");//4
		else if(pct%4==3)
			System.out.print(" \\ ");//4
		System.out.print("[");//1
		
		for(int i=0;i<bar;i++){
			if(i<pos)
				System.out.print(COMPLETE);//1
			else
				System.out.print(INCOMPLETE);//1
			
		}
		System.out.print("]");//1
		System.out.printf(" %3d",pct);//4
		System.out.print("% ");
	}
	
	public static void finish(){
		tick((int) _total);
	}
	
}
