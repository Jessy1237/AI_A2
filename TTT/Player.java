import java.util.Vector;

public class Player
{
    /**
     * Performs a move
     *
     * @param gameState the current state of the board
     * @param deadline time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play( final GameState gameState, final Deadline deadline )
    {
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves( nextStates );

        if ( nextStates.size() == 0 )
        {
            // Must play "pass" move if there are no other moves possible.
            return new GameState( gameState, new Move() );
        }

        /**
         * Here you should write your algorithms to get the best next move, i.e. the best next state. This skeleton returns a random move instead.
         */

        //Random random = new Random();
        //return nextStates.elementAt(random.nextInt(nextStates.size()));

        GameState bestState = null;
        int bestVal = 0;
        for ( GameState state : nextStates )
        {
            int tempVal = miniMax( state, gameState.getNextPlayer() );
            if ( tempVal > bestVal )
            {
                bestVal = tempVal;
                bestState = state;
            }
        }

        return bestState;
    }

    /**
     * A basic evaluation function for TTT
     * 
     * @param state The current state
     * @param player The current player
     * @return
     */
    private int eval( GameState state, int player )
    {
        int num = 0;

        //Check rows
        for ( int i = 0; i < 4; i++ )
        {
            for ( int j = 0; j < 4; j++ )
            {
                if ( state.at( i, j ) == player )
                    num++;
            }
        }

        //check cols
        for ( int j = 0; j < 4; j++ )
        {
            for ( int i = 0; i < 4; i++ )
            {
                if ( state.at( i, j ) == player )
                    num++;
            }
        }

        //check diags
        num += ( state.at( 0, 0 ) == player ) ? 1 : 0;
        num += ( state.at( 1, 1 ) == player ) ? 1 : 0;
        num += ( state.at( 2, 2 ) == player ) ? 1 : 0;
        num += ( state.at( 3, 3 ) == player ) ? 1 : 0;
        num += ( state.at( 3, 0 ) == player ) ? 1 : 0;
        num += ( state.at( 2, 1 ) == player ) ? 1 : 0;
        num += ( state.at( 1, 2 ) == player ) ? 1 : 0;
        num += ( state.at( 0, 3 ) == player ) ? 1 : 0;

        return num;
    }

    private int miniMax( GameState state, int player )
    {
        Vector<GameState> nextStates = new Vector<GameState>();
        state.findPossibleMoves( nextStates );

        if ( nextStates.size() == 0 ) //terminal state
        {
            return eval( state, player );
        }
        else
        {
            if ( player == Constants.CELL_X ) //player A
            {
                int best = Integer.MIN_VALUE;
                for ( GameState nextState : nextStates )
                {
                    int v = miniMax(nextState, state.getNextPlayer());
                    best = Math.max( best, v );
                }
                
                return best;
            }
            else //player B
            {
                int best = Integer.MAX_VALUE;
                for ( GameState nextState : nextStates )
                {
                    int v = miniMax(nextState, state.getNextPlayer());
                    best = Math.min( best, v );
                }
                
                return best;
            }
        }
    }
}
