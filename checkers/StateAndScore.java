public class StateAndScore {
 
    public GameState gameState;
    public int eval;

    public StateAndScore ( GameState gameState, int eval )
    {
    	this.gameState = gameState;
    	this.eval = eval;
    }

    public StateAndScore ( int eval )
    {
    	this.eval = eval;
    }
}