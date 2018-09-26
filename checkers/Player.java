import java.util.*;

public class Player {

    private int myPlayer;
    private final int MAXDEPTH = 2;
    private final int NUMTYPES = 4;
    private HashMap<Long, State> zobrist = new HashMap<>();
    private long[][] piece = new long[GameState.NUMBER_OF_SQUARES][NUMTYPES];

    public class State {
        int vmin;
        int vmax;
        int depth;
        long key;
        GameState gameState;
        public State(int depth, int valueMin, int valueMax, long key, GameState gameState) {
            this.depth = depth;
            this.vmin = valueMin;
            this.vmax = valueMax;
            this.key = key;
            this.gameState = gameState;
        }
    }

    /**
     * Performs a move
     *
     * @param pState
     *            the current state of the board
     * @param pDue
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState pState, final Deadline pDue) {

        Vector<GameState> lNextStates = new Vector<GameState>();
        pState.findPossibleMoves(lNextStates);

        myPlayer = pState.getNextPlayer();

        if (lNextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }

        /**
         * Here you should write your algorithms to get the best next move, i.e.
         * the best next state. This skeleton returns a random move instead.
         */
        //Random random = new Random();
        //return lNextStates.elementAt(random.nextInt(lNextStates.size()));

        GameState bestState = null;
        int beta = Integer.MAX_VALUE;
        int alpha = Integer.MIN_VALUE;

        Random rand = new Random();
        for ( int i = 0; i < GameState.NUMBER_OF_SQUARES; i++ )
        {
            for ( int j = 0; j < NUMTYPES; j++ )
            {
                piece[i][j] = rand.nextLong();
            }
        }

        StateAndScore temp = alphaBeta( pState, MAXDEPTH, alpha, beta, myPlayer );
        bestState = temp.gameState;
        return bestState;
    }

    /**
     * A basic evaluation function for checkers
     * 
     * @param state The current state
     * @param player The current player
     * @return The heuristic value for the given state and player
     */
    private int eval( GameState state, int player )
    {
        return 0;
    }

    private StateAndScore alphaBeta( GameState state, int depth, double alpha, double beta, int player )
    {
        Vector<GameState> nextStates = new Vector<GameState>();
        state.findPossibleMoves( nextStates );

        int other_player = ( player == Constants.CELL_RED ? Constants.CELL_WHITE : Constants.CELL_RED );

        long key = createKey(state);
        // this calculation has already been done
        if ( zobrist.containsKey(key) && zobrist.get(key).key == key )
        {
            if ( player == myPlayer )
                return new StateAndScore( zobrist.get(key).gameState, zobrist.get(key).vmax);
            return new StateAndScore( zobrist.get(key).gameState, zobrist.get(key).vmin);
        }

        StateAndScore bestChildAndVal = null;
        StateAndScore v = null;

        //int v = Integer.MAX_VALUE; // setting V for player B

        if ( nextStates.size() == 0 || depth == 0 ) //terminal state
        {

            //int temp = eval( state, myPlayer ) - eval( state, opponent );
            //System.err.println(temp);
            StateAndScore leaf = new StateAndScore( state, eval( state, myPlayer ) );
            return leaf;
        }

        else if ( player == myPlayer ) //Player A
        {
            v = new StateAndScore( Integer.MIN_VALUE );
            int min = Integer.MAX_VALUE;
            for ( GameState nextState : nextStates )
            {
                StateAndScore temp = alphaBeta( nextState, depth - 1, alpha, beta, other_player );
                if ( temp.eval > v.eval )
                {
                    v = temp;
                    if ( depth == MAXDEPTH )
                    {
                        bestChildAndVal = new StateAndScore( nextState, temp.eval );
                    }
                }
                min = Math.min( min, temp.eval );
                alpha = Math.max( alpha, v.eval );
                if ( beta <= alpha )
                    break; //Beta prune
            }
            zobrist.put(key, new State( MAXDEPTH-depth, min, v.eval, key, bestChildAndVal.gameState));
        }

        else //Player B
        {
            v = new StateAndScore( Integer.MAX_VALUE );
            int max = Integer.MIN_VALUE;
            for ( GameState nextState : nextStates )
            {
                StateAndScore temp = alphaBeta( nextState, depth - 1, alpha, beta, other_player );

                if ( temp.eval < v.eval )
                {
                    v = temp;
                    if ( depth == MAXDEPTH )
                    {
                        bestChildAndVal = new StateAndScore( nextState, temp.eval );
                    }
                }
                max = Math.max( max, temp.eval );
                beta = Math.min( beta, v.eval );
                if ( beta <= alpha )
                    break; //alpha prune
            }
            zobrist.put(key, new State( MAXDEPTH-depth, v.eval, max, key, bestChildAndVal.gameState));
        }

        if ( depth == MAXDEPTH ) //Allows us to get the best parent state i.e. best next move state
            return bestChildAndVal;

        return v;
    }

    public long createKey(GameState board) {
        long key = 0;
        int currentPiece;
        int index;
        
        // basic red = 0, basic white = 1, red king = 2, and white king = 3
        
        for ( int i = 0; i < GameState.NUMBER_OF_SQUARES; i++ )
        {   
            currentPiece = board.get(i);  
            if ( currentPiece != Constants.CELL_EMPTY )
            { 
                index = 0;
                if ( 0 != (currentPiece & Constants.CELL_KING) )
                    // king piece
                    index = 2;
                if ( 0 != (currentPiece & Constants.CELL_WHITE) )
                    // white piece
                    index++;
               
                key ^= piece[i][index];
            }
        }
       
        return key;
    }
}