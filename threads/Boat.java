package nachos.threads;
import nachos.ag.BoatGrader;
import nachos.machine.*;

public class Boat
{
    static BoatGrader bg;
    static Lock balsa;
    static boolean balsaLocation = true;
    static int asientos = 2;
    static Isla oahu;
    static Isla molokai;
    static Communicator com;
    private static final char dbgTread = 'b';
    

    public static  class Isla{
    	int nios;
    	int adultos;
    	int personas;
    	Condition isla;

    	public Isla(Lock isla){
    		this.isla = new Condition(isla);

    	}

    	public void setNios(int nios){
    		this.nios = nios;
    	}

    	public int getNios(){
    		return nios;
    	}

    	public void setAdultos(int adultos){
    		this.adultos = adultos;
    	}

    	public int getAdultos(){
    		return adultos;
    	}

    	public void setPersonas(int personas){
    		this.personas = personas;
    	}

    	public int getPersonas(){
    		return personas;
    	}

    	public void setIsla(Condition isla){
    		this.isla = isla;
    	}

    	public Condition getIsla(){
    		return isla;
    	}

    	public int todos(){
    		return adultos + nios;
    	}

    }



    public static void selfTest(){
		BoatGrader b = new BoatGrader();
		
		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(1, 0, b);
    //    begin(2, 5, b);

	//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
	//  	begin(1, 2, b);

	//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
	//  	begin(3, 3, b);
    }
    


    public static void begin( int adults, int children, BoatGrader b ){
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here
		balsa = new Lock();
		oahu = new Isla(balsa);
		molokai = new Isla(balsa);
		oahu.setNios(children);
		oahu.setAdultos(adults);
		molokai.setNios(0);
		molokai.setAdultos(0);

		
		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.





	    for (int i = 0; i < children; i++) {
		    Runnable nios_r = new Runnable() {
			    public void run() {
			    	boolean location = false;
		            ChildItinerary();
		        }
		    };
        	KThread t = new KThread(nios_r);
        	t.setName("Boat Thread - Child - #" + (i+1));

        	t.fork();
    	}
     
		for (int i = 0; i < adults; i++) {

		    Runnable adultos_r = new Runnable() {
			    public void run() {
			    	boolean location = true;
		            AdultItinerary();
		        }
		    };
		    KThread t = new KThread(adultos_r);
		    t.setName("Boat Thread - Adult - #" + (i+1));
		    t.fork();
		}
			    
        com = new Communicator();
        com.listen();

    }

    static void AdultItinerary()
    {
		/* This is where you should put your solutions. Make calls
		   to the BoatGrader to show that it is synchronized. For
		   example:
		       bg.AdultRowToMolokai();
		   indicates that an adult has rowed the boat across to Molokai
		*/

		boolean location = true;
		balsa.acquire();

		while(true){
			if (location){

				if (balsaLocation){
					if(!(asientos >1)){
						Lib.debug(dbgTread, "no hay asientos para adultos");
						oahu.getIsla().sleep();
					}
					else if(oahu.getNios()>=2){
						Lib.debug(dbgTread, "los nios pasan primero");
						oahu.getIsla().sleep();
					}
					else{
						location = false;
						asientos = 0;
						oahu.setAdultos(oahu.getAdultos()-1);
						//Lib.debug(dbgTread, KThread.currentTread().getName() + "rema");
						bg.AdultRowToMolokai();
						molokai.setAdultos(molokai.getAdultos()+1);
						molokai.setPersonas(oahu.todos());
						molokai.getIsla().wakeAll();
						asientos = 2;
						balsaLocation = false;
						molokai.getIsla().sleep();

					}
				}
				else{
					oahu.getIsla().sleep();
				}
			}
			else{
				molokai.getIsla().sleep();
			}
		}
		

    }

    static void ChildItinerary(){
    	boolean location = true;

    	balsa.acquire();
    	Lib.debug(dbgTread, "voy a comenzar nios");

    	while(true){
    		if(location){
    			if(balsaLocation){
    				if(oahu.getNios()<2){
    					oahu.getIsla().sleep();
    				}

    				if(asientos>1){
    					location = false;
    					oahu.setNios(oahu.getNios()-1);
    					asientos = 1;
    					bg.ChildRowToMolokai();

    					if (oahu.getNios() > 0){
    						oahu.setNios(oahu.getNios()+1);
    						oahu.getIsla().wakeAll();
    					}
    					else{
    						balsaLocation = false;
    						asientos = 2;
    						molokai.setNios(molokai.getNios()+1);
    						molokai.setPersonas(oahu.todos());
    						molokai.getIsla().wakeAll();

    					}
    					molokai.getIsla().sleep();
    				}

    				else if(asientos == 1){
    					asientos = 0;
    					oahu.setNios(oahu.getNios()-2);
    					bg.ChildRideToMolokai();
    					molokai.setNios(molokai.getNios()+2);
    					molokai.setPersonas(oahu.todos());
    					asientos = 2;
    					location = false;
    					balsaLocation = false;
    					molokai.getIsla().wakeAll();
    					molokai.getIsla().sleep();

    				}
    				else{
    					oahu.getIsla().sleep();
    				}
    			}
    			else{
    				oahu.getIsla().sleep();
    			}
    		}
    		else{
    			if(molokai.getPersonas() == 0){
    				Lib.debug(dbgTread, "ya no hay gente en oahu");
    				System.out.println("Termine");
    				com.speak(2);
    				// molokai.getIsla().sleep();
    			}
    			else{
    				if(!balsaLocation){
    					Lib.debug(dbgTread,"ninio llego a oahu de nuevo");
    					location = true;
    					molokai.setNios(molokai.getNios()-1);
    					asientos = 1;
    					bg.ChildRowToOahu();
    					oahu.setNios(oahu.getNios()+1);
    					oahu.setPersonas(molokai.getPersonas());
    					asientos = 2;
    					balsaLocation = true;
    					oahu.getIsla().wakeAll();
    					oahu.getIsla().sleep();
    				}
    				else{
    					molokai.getIsla().sleep();
    				}
    			}
    		}

    	}

    }


    static void SampleItinerary()
    {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
    }


        
    
}
