import java.util.Vector;

public class Player
{

    int myPlayer;
    int opponent;
    final int MAXDEPTH = 2;

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
        myPlayer = (gameState.getNextPlayer() == Constants.CELL_X ? Constants.CELL_O : Constants.CELL_X);
        opponent = gameState.getNextPlayer();

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

/*
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
                int temp = alphaBeta( state, MAXDEPTH, alpha, beta, myPlayer );
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
                int temp = alphaBeta( state, MAXDEPTH, alpha, beta, myPlayer );
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
*/
        StateAndScore temp = alphaBeta( gameState, MAXDEPTH, alpha, beta, myPlayer );
        bestState = temp.gameState;
        return bestState;
    }

    private int[] calc2dDiagonals ( GameState state, int player, int constantDim, int layer )
    {
        int[] sums = new int[2];
        int numDiaPAL = 0;
        int numDiaPBL = 0;
        int numDiaPAR = 0;
        int numDiaPBR = 0;
        for (int j = 0; j < GameState.BOARD_SIZE; j++)
        {
            int temp = getGameState(state, j, j, constantDim, layer);
            if ( temp == player )
            {
                numDiaPAL++;
                numDiaPBL = Integer.MIN_VALUE;
            }
            else if ( temp != Constants.CELL_EMPTY )
            {
                numDiaPBL++;
                numDiaPAL = Integer.MIN_VALUE;
            }

            temp = getGameState(state, j, (GameState.BOARD_SIZE - 1) - j, constantDim, layer);
            if (temp == player)
            {
                numDiaPAR++;
                numDiaPBR = Integer.MIN_VALUE;
            }
            else if ( temp != Constants.CELL_EMPTY )
            {
                numDiaPBR++;
                numDiaPAR = Integer.MIN_VALUE;
            }
        }
        sums[0] = numDiaPAL + numDiaPAR;
        sums[1] = numDiaPBL + numDiaPBR;
        return sums;
    }

    private int[] calc3dDiagonals ( GameState state, int player )
    {
        int d1PA = 0;
        int d1PB = 0;
        int d2PA = 0;
        int d2PB = 0;
        int d3PA = 0;
        int d3PB = 0;
        int d4PA = 0;
        int d4PB = 0;

        for ( int j = 0; j < GameState.BOARD_SIZE; j++ )
        {
            int temp = state.at(j, j, j);
            if ( temp == player )
            {
                d1PA++;
                d1PB = Integer.MIN_VALUE;
            }
            else if ( temp != Constants.CELL_EMPTY )
            {
                d1PB++;
                d1PA = Integer.MIN_VALUE;
            }

            temp = state.at(j, j, (GameState.BOARD_SIZE - 1) - j);
            if ( temp == player )
            {
                d2PA++;
                d2PB = Integer.MIN_VALUE;
            }
            else if ( temp != Constants.CELL_EMPTY )
            {
                d2PB++;
                d2PA = Integer.MIN_VALUE;
            }

            temp = state.at(j, (GameState.BOARD_SIZE - 1) - j, j);
            if ( temp == player )
            {
                d3PA++;
                d3PB = Integer.MIN_VALUE;
            }
            else if ( temp != Constants.CELL_EMPTY )
            {
                d3PB++;
                d3PA = Integer.MIN_VALUE;
            }

            temp = state.at(j, (GameState.BOARD_SIZE - 1) - j, (GameState.BOARD_SIZE - 1) - j);
            if ( temp == player )
            {
                d4PA++;
                d4PB = Integer.MIN_VALUE;
            }
            else if ( temp != Constants.CELL_EMPTY )
            {
                d4PB++;
                d4PA = Integer.MIN_VALUE;
            }
        }
        int[] sums = new int[2];
        sums[0] = d1PA + d2PA + d3PA + d4PA;
        sums[1] = d1PB + d2PB + d3PB + d4PB;
        return sums;
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
        int sumPA = 0;
        int sumPB = 0;
        
        //Check rows, columns, and layers for straight lines
        for( int k = 0; k < GameState.BOARD_SIZE; k++ )
        {
            for( int i = 0; i < GameState.BOARD_SIZE; i++ )
            {
                int widthA = 0;
                int widthB = 0;
                int lengthA = 0;
                int lengthB = 0;
                int depthA = 0;
                int depthB = 0;
                for( int j = 0; j < GameState.BOARD_SIZE; j++ )
                {
                    // width
                    int temp = state.at(i, j, k);
                    if ( temp == player )
                    {
                        widthA++;
                        widthB = Integer.MIN_VALUE;
                    }
                    else if ( temp != Constants.CELL_EMPTY )
                    {
                        widthB++;
                        widthA = Integer.MIN_VALUE;
                    }

                    // length
                    temp = state.at(j, i, k);
                    if ( temp == player )
                    {
                        lengthA++;
                        lengthB = Integer.MIN_VALUE;
                    }
                    else if ( temp != Constants.CELL_EMPTY )
                    {
                        lengthB++;
                        lengthA = Integer.MIN_VALUE;
                    }

                    // depth
                    temp = state.at(k, i, j);
                    if ( temp == player )
                    {
                        depthA++;
                        depthB = Integer.MIN_VALUE;
                    }
                    else if ( temp != Constants.CELL_EMPTY )
                    {
                        depthB++;
                        depthA = Integer.MIN_VALUE;
                    }
                }
                sumPA += widthA + lengthA + depthA;
                sumPB += widthB + lengthB + depthB;
            }

        }

        // Check diagonals of a single depth
        for ( int k = 0; k < 3; k++ )
        {
            for ( int i = 0; i < GameState.BOARD_SIZE; i++ )
            {
                int[] diagSums = calc2dDiagonals(state, player, k, i);
                sumPA += diagSums[0];
                sumPB += diagSums[1];
            }
        }
        
        // Check diagonals of multple layers
        int[] diagSums = calc3dDiagonals(state, player);
        sumPA += diagSums[0];
        sumPB += diagSums[1];
        
        return sumPA - sumPB;
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

    private StateAndScore alphaBeta( GameState state, int depth, int alpha, int beta, int player )
    {
        Vector<GameState> nextStates = new Vector<GameState>();
        state.findPossibleMoves( nextStates );

        int other_player = (player == Constants.CELL_X ? Constants.CELL_O : Constants.CELL_X);

        StateAndScore bestChildAndVal = null;
        StateAndScore v = null;

        //int v = Integer.MAX_VALUE; // setting V for player B

        if ( nextStates.size() == 0 || depth == 0 ) //terminal state
        {

            //int temp = eval( state, myPlayer ) - eval( state, opponent );
            //System.err.println(temp);
            StateAndScore leaf = new StateAndScore( state, eval(state, myPlayer ));
            return leaf;
        }

        else if ( player == myPlayer ) //Player A
        {
            v = new StateAndScore(Integer.MIN_VALUE);
            for ( GameState nextState : nextStates )
            {
                StateAndScore temp = alphaBeta( nextState, depth - 1, alpha, beta, other_player );
                if ( temp.eval > v.eval )
                {
                    v = temp;
                    alpha = Math.max( alpha, v.eval );
                    if ( depth == MAXDEPTH )
                    {
                        bestChildAndVal = new StateAndScore( nextState, temp.eval );
                    }
                    if ( beta <= alpha )
                        break; //Beta prune
                }
            }
        }

        else //Player B
        {
            v = new StateAndScore(Integer.MAX_VALUE);
            for ( GameState nextState : nextStates )
            {
                StateAndScore temp = alphaBeta( nextState, depth - 1, alpha, beta, other_player );

                if ( temp.eval < v.eval )
                {
                    v = temp;
                    beta = Math.min( beta, v.eval );
                    if ( depth == MAXDEPTH )
                    {
                        bestChildAndVal = new StateAndScore( nextState, temp.eval );
                    }
                    if ( beta <= alpha )
                        break; //alpha prune
                }
            }
        }
        if ( depth == MAXDEPTH )
            return bestChildAndVal;

        return v;
    }

    private int getGameState ( GameState state, int a, int b, int constantDim, int layer )
    {
        switch(constantDim)
        {
            case 0:
            return state.at(layer, a, b);
            case 1:
            return state.at(a, layer, b);
            case 2:
            return state.at(a, b, layer);
            default:
            System.err.println("invalid switch entry");
        }
        return 0;
    }
}
