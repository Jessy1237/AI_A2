import java.util.Vector;

public class Player
{

    private int myPlayer;
    private final int MAXDEPTH = 2;
    //private int numPreviousMoves = 0;

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

        // count the number of moves already played before this one
        // myPlayer and opponent moves counted separately
        /*if ( numPreviousMoves == 0 )
        {
            for ( int k = 0; k < GameState.BOARD_SIZE; k++ )
            {
                for ( int i = 0; i < GameState.BOARD_SIZE; i++ )
                {
                    for ( int j = 0; j < GameState.BOARD_SIZE; j++ )
                    {
                        int temp = gameState.at(i, j, k);
                        if ( temp == Constants.CELL_X || temp == Constants.CELL_O )
                            numPreviousMoves++;
                    }
                }
            }
        }
        else
            numPreviousMoves += 2;*/

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

    private int[] calc2dDiagonals( GameState state, int player, int constantDim, int layer )
    {
        int[] sums = new int[2];
        int numDiaPAL = 0;
        int numDiaPBL = 0;
        int numDiaPAR = 0;
        int numDiaPBR = 0;
        for ( int j = 0; j < GameState.BOARD_SIZE; j++ )
        {
            int temp = getGameState( state, j, j, constantDim, layer );
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

            temp = getGameState( state, j, ( GameState.BOARD_SIZE - 1 ) - j, constantDim, layer );
            if ( temp == player )
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
        sums[0] = calcScore( numDiaPAL ) + calcScore( numDiaPAR );
        sums[1] = calcScore( numDiaPBL ) + calcScore( numDiaPBR );
        return sums;
    }

    private int[][] calc3dDiagonals( GameState state, int player, int[][] diags, int j )
    {
        int temp = state.at( j, j, j );
        if ( temp == player )
        {
            diags[0][0]++;
            diags[1][0] = Integer.MIN_VALUE;
        }
        else if ( temp != Constants.CELL_EMPTY )
        {
            diags[1][0]++;
            diags[0][0] = Integer.MIN_VALUE;
        }

        temp = state.at( j, j, ( GameState.BOARD_SIZE - 1 ) - j );
        if ( temp == player )
        {
            diags[0][1]++;
            diags[1][1] = Integer.MIN_VALUE;
        }
        else if ( temp != Constants.CELL_EMPTY )
        {
            diags[1][1]++;
            diags[0][1] = Integer.MIN_VALUE;
        }

        temp = state.at( j, ( GameState.BOARD_SIZE - 1 ) - j, j );
        if ( temp == player )
        {
            diags[0][2]++;
            diags[1][2] = Integer.MIN_VALUE;
        }
        else if ( temp != Constants.CELL_EMPTY )
        {
            diags[1][2]++;
            diags[1][2] = Integer.MIN_VALUE;
        }

        temp = state.at( j, ( GameState.BOARD_SIZE - 1 ) - j, ( GameState.BOARD_SIZE - 1 ) - j );
        if ( temp == player )
        {
            diags[0][3]++;
            diags[1][3] = Integer.MIN_VALUE;
        }
        else if ( temp != Constants.CELL_EMPTY )
        {
            diags[1][3]++;
            diags[0][3] = Integer.MIN_VALUE;
        }

        return diags;
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
        int diag3d[][] = new int[2][4]; //used to calculate the values of the 3d diagonals

        if ( state.isEOG() ) //If the state is end of game then there is no need to calculate values we can output +-inf 
        {
            if ( ( player == Constants.CELL_X && state.isXWin() ) || ( player == Constants.CELL_O && state.isOWin() ) ) //Player wins
            {
                return Integer.MAX_VALUE;
            }
            else if ( !state.isXWin() && !( state.isOWin() ) ) //Draw
            {
                return Integer.MIN_VALUE / 4;
            }
            else
            {
                return Integer.MIN_VALUE;
            }
        }

        //Check rows, columns, and layers for straight lines
        for ( int k = 0; k < GameState.BOARD_SIZE; k++ )
        {
            //Checking of the 3d diagonals
            calc3dDiagonals( state, player, diag3d, k );

            for ( int i = 0; i < GameState.BOARD_SIZE; i++ )
            {
                int widthA = 0;
                int widthB = 0;
                int lengthA = 0;
                int lengthB = 0;
                int depthA = 0;
                int depthB = 0;
                for ( int j = 0; j < GameState.BOARD_SIZE; j++ )
                {
                    // width
                    int temp = state.at( i, j, k );
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
                    temp = state.at( j, i, k );
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
                    temp = state.at( k, i, j );
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
                sumPA += calcScore( widthA ) + calcScore( lengthA ) + calcScore( depthA );
                sumPB += calcScore( widthB ) + calcScore( lengthB ) + calcScore( depthB );
            }

        }

        // Check diagonals of a single depth
        for ( int k = 0; k < 3; k++ )
        {
            for ( int i = 0; i < GameState.BOARD_SIZE; i++ )
            {
                int[] diagSums = calc2dDiagonals( state, player, k, i );
                sumPA += diagSums[0];
                sumPB += diagSums[1];
            }
        }

        //Calc 3D diags score     
        sumPA += calcScore( diag3d[0][0] ) + calcScore( diag3d[0][1] ) + calcScore( diag3d[0][2] ) + calcScore( diag3d[0][3] );
        sumPB += calcScore( diag3d[1][0] ) + calcScore( diag3d[1][1] ) + calcScore( diag3d[1][2] ) + calcScore( diag3d[1][3] );

        return sumPA - sumPB;
    }

    private int calcScore( int moves )
    {
        if ( moves <= 0 )
            return 0;

        int points = 0;
        // winning move!!
        if ( moves == 4 )
        {
            points = 10000000;
        }
        else if ( moves > 0 )
            points = ( int ) Math.pow( 10, moves );

        return points;
    }

    private StateAndScore alphaBeta( GameState state, int depth, double alpha, double beta, int player )
    {
        Vector<GameState> nextStates = new Vector<GameState>();
        state.findPossibleMoves( nextStates );

        int other_player = ( player == Constants.CELL_X ? Constants.CELL_O : Constants.CELL_X );

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
                alpha = Math.max( alpha, v.eval );
                if ( beta <= alpha )
                    break; //Beta prune
            }
        }

        else //Player B
        {
            v = new StateAndScore( Integer.MAX_VALUE );
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
                beta = Math.min( beta, v.eval );
                if ( beta <= alpha )
                    break; //alpha prune
            }
        }

        if ( depth == MAXDEPTH ) //Allows us to get the best parent state i.e. best next move state
            return bestChildAndVal;

        return v;
    }

    private int getGameState( GameState state, int a, int b, int constantDim, int layer )
    {
        switch ( constantDim )
        {
            case 0:
                return state.at( layer, a, b );
            case 1:
                return state.at( a, layer, b );
            case 2:
                return state.at( a, b, layer );
            default:
                System.err.println( "invalid switch entry" );
        }
        return 0;
    }
}
