import java.util.Vector;

public class Player
{

    int myPlayer;

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
        myPlayer = gameState.getNextPlayer();

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
        int beta = Integer.MAX_VALUE;
        int alpha = Integer.MIN_VALUE;
        int maxDepth = 100;

        if ( gameState.getNextPlayer() == Constants.CELL_X ) // Player A i.e. its the other players turn next
        {
            int v = Integer.MIN_VALUE;
            for ( GameState state : nextStates )
            {
                //    int tempVal = miniMax( state, gameState.getNextPlayer() );
                //    if ( tempVal > bestVal )
                //    {
                //        bestVal = tempVal;
                //        bestState = state;
                //    }
                int temp = alphaBeta( state, maxDepth, alpha, beta, myPlayer );
                if ( temp > v ) //equiv to Math.max but also gives us the best state
                {
                    v = temp;
                    bestState = state;
                }

                alpha = Math.max( alpha, v );

                if ( beta <= alpha )
                    break;
            }
        }
        else //Player B
        {
            int v = Integer.MAX_VALUE;
            for ( GameState state : nextStates )
            {
                int temp = alphaBeta( state, maxDepth, alpha, beta, myPlayer );
                if ( temp < v ) //equiv to Math.min but also gives us the best state
                {
                    v = temp;
                    bestState = state;
                }

                beta = Math.min( beta, v );

                if ( beta <= alpha )
                    break;
            }
        }

        return bestState;
    }

    /**
     * Calculates the amount of score the current line is worth for the current player. Negative means this state is in favour of the opponent (Player B)
     * 
     * @param numPA number of marks the current player has in this line (Player A)
     * @param numPB number of marks the opponent has in the line (Player B)
     * @return a heuristic value for the current line based off the number of marks each player has
     */
    private int calcScore( int numPA, int numPB )
    {
        int score = 0;

        if ( numPA > 0 && numPB > 0 ) //Do nothing as this line is a draw
        {
        }
        else if ( numPB == 0 ) // Add points as our opponent has no marks in this line
        {
            score = 1;
            for ( int i = 1; i < numPA; i++ )
            {
                score *= 10;
            }
        }
        else if ( numPA == 0 )// Subtract points as our opponent has no marks in this line
        {
            score = -1;
            for ( int i = 1; i < numPA; i++ )
            {
                score *= 10;
            }
        }

        return score;
    }

    /**
     * A basic evaluation function for TTT
     * 
     * @param state The current state
     * @param player The current player
     * @return The heuristic value for the given state and player
     */
    private int eval( GameState state, int player )
    {
        int numPA = 0;
        int numPB = 0;
        int score = 0;

/*
        if ( state.isEOG() ) //terminal state
        {
            if ( ( player == Constants.CELL_X && state.isXWin() ) || ( player == Constants.CELL_O && state.isOWin() ) )
            {
                return Integer.MAX_VALUE;
            }
            else
            {
                return Integer.MIN_VALUE; //Regardless of which player the current player lost
            }
        }
*/

        int sum = 0;
        //Check rows
        for ( int i = 0; i < GameState.BOARD_SIZE; i++ )
        {
            numPA = 0;
            numPB = 0;
            // number of spots PA has in this row
            int numRowPA = 1;
            for ( int j = 0; j < GameState.BOARD_SIZE; j++ )
            {
                if ( state.at( i, j ) == player )
                {
                    //numPA++;
                    numPA += numRowPA * 5;
                    numRowPA++;
                }
                else if ( state.at( i, j ) != Constants.CELL_EMPTY )
                {
                    //numPB++;
                    numPA = 0;
                    break;
                }
            }
            sum += numPA;
            score += calcScore( numPA, numPB );
        }

        //check cols
        for ( int j = 0; j < GameState.BOARD_SIZE; j++ )
        {
            numPA = 0;
            numPB = 0;
            int numColPA = 1;
            for ( int i = 0; i < GameState.BOARD_SIZE; i++ )
            {
                if ( state.at( i, j ) == player )
                {
                    //numPA++;
                    numPA += numColPA * 5;
                    numColPA++;
                }
                else if ( state.at( i, j ) != Constants.CELL_EMPTY )
                {
                    //numPB++;
                    numPA = 0;
                    break;
                }
            }
            sum += numPA;
            score += calcScore( numPA, numPB );
        }

        numPA = 0;
        numPB = 0;
        int numDiaPAL = 1;
        //check Left-Right diag
        for ( int i = 0; i < GameState.BOARD_SIZE; i++ )
        {
            if ( state.at( i, i ) == player )
            {
                //numPA++;
                numPA += numDiaPAL * 5;
                numDiaPAL++;
            }
            else if ( state.at( i, i ) != Constants.CELL_EMPTY )
            {
                //numPB++;
                numPA = 0;
                break;
            }
        }
        sum += numPA;

        numPA = 0;
        numPB = 0;
        int numDiaPAR = 1;
        // check Right-Left diag
        for ( int i = 0; i < GameState.BOARD_SIZE; i++ )
        {
            if ( state.at( i, ( GameState.BOARD_SIZE - 1 ) - i ) == player )
            {
                //numPA++;
                numPA += numDiaPAR * 5;
                numDiaPAR++;
            }
            else if ( state.at( i, ( GameState.BOARD_SIZE - 1 ) - i ) != Constants.CELL_EMPTY )
            {
                //numPB++;
                numPA = 0;
                break;
            }
        }
        sum += numPA;
        //score += calcScore( numPA, numPB );

        return sum;
    }

    @SuppressWarnings( "unused" )
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
                    int v = miniMax( nextState, state.getNextPlayer() );
                    best = Math.max( best, v );
                }

                return best;
            }
            else //player B
            {
                int best = Integer.MAX_VALUE;
                for ( GameState nextState : nextStates )
                {
                    int v = miniMax( nextState, state.getNextPlayer() );
                    best = Math.min( best, v );
                }

                return best;
            }
        }
    }

    private int alphaBeta( GameState state, int depth, int alpha, int beta, int player )
    {
        Vector<GameState> nextStates = new Vector<GameState>();
        state.findPossibleMoves( nextStates );

        int v = Integer.MAX_VALUE; // setting V for player B

        if ( nextStates.size() == 0 || depth == 0 ) //terminal state
        {
            //return eval( state, player );

            int opponent = Constants.CELL_X;
            if ( myPlayer == Constants.CELL_X )
                opponent = Constants.CELL_O;

            int temp = eval( state, myPlayer ) - eval( state, opponent );
            //System.err.println(temp);
            return temp;
        }

        else if ( player == Constants.CELL_X ) //Player A
        {
            v = Integer.MIN_VALUE;
            for ( GameState nextState : nextStates )
            {
                v = Math.max( v, alphaBeta( nextState, depth - 1, alpha, beta, Constants.CELL_O ) );
                alpha = Math.max( alpha, v );
                if ( beta <= alpha )
                    break; //Beta prune
            }
        }

        else //Player B
        {
            for ( GameState nextState : nextStates )
            {
                v = Math.min( v, alphaBeta( nextState, depth - 1, alpha, beta, Constants.CELL_X ) );
                beta = Math.min( beta, v );
                if ( beta <= alpha )
                    break; //alpha prune
            }
        }

        return v;
    }
}
