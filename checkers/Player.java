import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class Player
{

    private int myPlayer;
    private final int MAXDEPTH = 9;
    private final int NUMTYPES = 4;

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
            if ( 0 != ( temp & Constants.CELL_RED ) )
            {
                numRed++;
                // give more points to pieces on the opponent's side of the board
                if ( i >= WHITESIDE )
                    numRed += 5;
                // corner pieces cannot get jumped
                if ( remainder == 0 || remainder == 3 )
                    numRed += 2;
                if ( 0 != ( temp & Constants.CELL_KING ) )
                    numRed += 20;
            }
            else if ( 0 != ( temp & Constants.CELL_WHITE ) )
            {
                numWhite++;
                if ( i <= REDSIDE )
                    numWhite += 5;
                if ( remainder == 0 || remainder == 3 )
                    numWhite += 2;
                if ( 0 != ( temp & Constants.CELL_KING ) )
                    numWhite += 20;
            }
        }
        int diff = numWhite - numRed;
        if ( player == Constants.CELL_RED )
            diff = numRed - numWhite;

        // todo: implement some look aheads and favor almost kings

        return diff;
    }

    /**
     * recursive method that creates and evaluates the game tree up to a max depth Adapted from course pseudo code
     * 
     * @return the best GameState and associated eval value we found for the next move
     */
    private StateAndScore alphaBeta( GameState state, int depth, int alpha, int beta, int player )
    {
        Vector<GameState> nextStates = new Vector<GameState>();
        state.findPossibleMoves( nextStates );

        int other_player = ( player == Constants.CELL_RED ? Constants.CELL_WHITE : Constants.CELL_RED );

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
            Vector<GameState> orderedStates = orderStates( nextStates, true );
            for ( GameState nextState : orderedStates )
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
        }

        else //Player B
        {
            v = new StateAndScore( Integer.MAX_VALUE );
            int max = Integer.MIN_VALUE;
            Vector<GameState> orderedStates = orderStates( nextStates, false );
            for ( GameState nextState : orderedStates )
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
        }

        if ( depth == MAXDEPTH ) //Allows us to get the best parent state i.e. best next move state
            return bestChildAndVal;

        return v;
    }

    /**
     * Method orders the child states of this GameState in ascending or descendinig order
     * 
     * @param nextStates the child states to be sorted
     * @return the child states in order according to descending T/F
     */
    public Vector<GameState> orderStates( Vector<GameState> nextStates, boolean descending )
    {
        Collections.sort( nextStates, new Comparator<GameState>() {
            @Override
            public int compare( GameState s1, GameState s2 )
            {
                return Integer.compare( eval( s1, myPlayer ), eval( s2, myPlayer ) );
            }
        } );
        if ( descending )
        {
            Collections.reverse( nextStates );
        }
        return nextStates;
    }
}