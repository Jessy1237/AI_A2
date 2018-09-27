import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class Player
{

    private int myPlayer;
    private final int MAXDEPTH = 13;
    private final int NUMTYPES = 4;
    private HashMap<Long, State> zobrist = new HashMap<>();
    private long[][] piece = new long[GameState.NUMBER_OF_SQUARES][NUMTYPES];

    /**
     * Performs a move
     *
     * @param pState the current state of the board
     * @param pDue time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play( final GameState pState, final Deadline pDue )
    {

        Vector<GameState> lNextStates = new Vector<GameState>();
        pState.findPossibleMoves( lNextStates );

        myPlayer = pState.getNextPlayer();

        if ( lNextStates.size() == 0 )
        {
            // Must play "pass" move if there are no other moves possible.
            return new GameState( pState, new Move() );
        }

        /**
         * Here you should write your algorithms to get the best next move, i.e. the best next state. This skeleton returns a random move instead.
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
        int numWhite = 0;
        int numRed = 0;
        // board positions for starting player sides
        final int REDSIDE = 11;
        final int WHITESIDE = 20;

        if ( state.isEOG() )
        {
            if ( ( player == Constants.CELL_RED && state.isRedWin() ) || ( player == Constants.CELL_WHITE && state.isWhiteWin() ) )
            {
                return Integer.MAX_VALUE;
            }
            else if ( state.isDraw() )
            {
                return 0;
            }
            else
            {
                return Integer.MIN_VALUE;
            }
        }

        for ( int i = 0; i < GameState.NUMBER_OF_SQUARES; i++ )
        {
            int temp = state.get( i );
            int remainder = i % NUMTYPES;
            if ( 0 != (temp & Constants.CELL_RED) )
            {
                numRed++;
                // give more points to pieces on the opponent's side of the board
                if ( i >= WHITESIDE )
                    numRed += 5;
                // corner pieces cannot get jumped
                if ( remainder == 0 || remainder == 3 )
                    numRed += 2;
                if ( 0 != (temp & Constants.CELL_KING) )
                    numRed += 20;
            }
            else // White player
            {
                numWhite++;
                if ( i <= REDSIDE )
                    numWhite += 5;
                if ( remainder == 0 || remainder == 3 )
                    numWhite += 2;
                if ( 0 != (temp & Constants.CELL_KING) )
                    numWhite += 20;
            }
        }
        int diff = numWhite - numRed;
        if ( player == Constants.CELL_RED )
            diff = numRed - numWhite;

        diff *= 100;

        // todo: implement some look aheads and favor almost kings

        return diff;
    }

    /**
     * recursive method that creates and evaluates the game tree up to a max depth
     * Adapted from course pseudo code
     * @return the best GameState and associated eval value we found for the next move
     */
    private StateAndScore alphaBeta( GameState state, int depth, int alpha, int beta, int player )
    {
        Vector<GameState> nextStates = new Vector<GameState>();
        state.findPossibleMoves( nextStates );

        int other_player = ( player == Constants.CELL_RED ? Constants.CELL_WHITE : Constants.CELL_RED );

        long key = createKey( state );
        // this calculation has already been done
        if ( zobrist.containsKey( key ) && zobrist.get( key ).key == key )
        {
            if ( player == myPlayer )
                return new StateAndScore( state, zobrist.get( key ).vmax );
            return new StateAndScore( state, zobrist.get( key ).vmin );
        }

        StateAndScore bestChildAndVal = null;
        StateAndScore v = null;

        if ( nextStates.size() == 0 || depth == 0 ) //terminal state
        {
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
            zobrist.put( key, new State( MAXDEPTH - depth, min, v.eval, key ) );
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
            zobrist.put( key, new State( MAXDEPTH - depth, v.eval, max, key ) );
        }

        if ( depth == MAXDEPTH ) //Allows us to get the best parent state i.e. best next move state
            return bestChildAndVal;

        return v;
    }

    /**
     * method that creates a unique hash key for the current board state
     * @param board the current GameState
     * @return key a long representing the current board state
     */
    public long createKey( GameState board )
    {
        long key = 0;
        int currentPiece;
        int index;

        // basic red = 0, basic white = 1, red king = 2, and white king = 3

        for ( int i = 0; i < GameState.NUMBER_OF_SQUARES; i++ )
        {
            currentPiece = board.get( i );
            if ( currentPiece != Constants.CELL_EMPTY )
            {
                index = 0;
                if ( 0 != ( currentPiece & Constants.CELL_KING ) )
                    index = 2; // king piece
                if ( 0 != ( currentPiece & Constants.CELL_WHITE ) )
                    index++; // white piece

                key ^= piece[i][index];
            }
        }

        return key;
    }

    /**
     * Class for the value in the zobrist hash map
     */
    public class State
    {
        int vmin;
        int vmax;
        int depth;
        long key;

        public State( int depth, int valueMin, int valueMax, long key )
        {
            this.depth = depth;
            this.vmin = valueMin;
            this.vmax = valueMax;
            this.key = key;
        }
    }
}