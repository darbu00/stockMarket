/*
 * Stock Market
 * 
 * Ported to Java by David Arbuthnot
 *
 * Based on the original Basic game of Stock Market available here
 * https://github.com/coding-horror/basic-computer-games/blob/main/83_Stock_Market/stockmarket.bas
 * 
 * Note:  My goal was to create a version of the 1970's game in Java, without adding any
 * new features and staying as true as possible to the original game play including
 * display formatting, etc.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class StockMarket {
	
	private final static double START_CASH = 10000.00d;
	private final static double BROKERAGE_FEE = 0.01;
	
    private enum GAME_STATE {
        STARTUP,
        INIT,
        TRADE,
        END_OF_DAY,
        END_GAME,
        GAME_OVER
    }

    //Assets and stocks
	private ArrayList<Asset> assets;
	private ArrayList<Stock> stocks;
	
	//Set starting cash value
    private double cash = START_CASH;
    
    //Game system variables
    private double slope;
    private int daysTrending;
    private int bigChangeUpIndex;
    private int bigChangeUpDays = 0;
    private int bigChangeDownIndex;
    private int bigChangeDownDays = 0;
    
    //keyboard scanner for user input
    Scanner kbScanner = new Scanner(System.in);
    
    
    public void playStockMarket() {

		// Current game state
		GAME_STATE gameState = GAME_STATE.INIT;

        do {
            switch (gameState) {

                case INIT:
                	//Display game title and credits
                	System.out.println("" + generateTabs(30) + "STOCK MARKET");
                	System.out.println(generateTabs(15) + "CREATIVE COMPUTING  MORRISTOWN, NEW JERSEY");
                	System.out.println("\n\n\n");
                	//Display instructions?
					/*
					The original game in Basic had:
					DO YOU WANT THE INSTRUCTIONS (YES-TYPE Y1, NO-TYPE 0)?
					I have changed this to be more logical and consistent with the
					change made to the DO YOU WISH TO CONTINUE question during actual
					game play.
					*/
                	System.out.print("DO YOU WANT THE INSTRUCTIONS (YES-TYPE Y, NO-TYPE N)?");
                	boolean yesOrNo = getYesOrNoInput();
                	System.out.println("\n\n");
                	if (yesOrNo) {
                		System.out.print("""
								THIS PROGRAM PLAYS THE STOCK MARKET.  YOU WILL BE GIVEN
								$10,000 AND MAY BUY OR SELL STOCKS.  THE STOCK PRICES WILL
								BE GENERATED RANDOMLY AND THEREFORE THIS MODEL DOES NOT
								REPRESENT EXACTLY WHAT HAPPENS ON THE EXCHANGE.  A TABLE
								OF AVAILABLE STOCKS, THEIR PRICES, AND THE NUMBER OF SHARES
								IN YOUR PORTFOLIO WILL BE PRINTED.  FOLLOWING THIS, THE
								INITIALS OF EACH STOCK WILL BE PRINTED WITH A QUESTION
								MARK.  HERE YOU INDICATE A TRANSACTION.  TO BUY A STOCK
								TYPE +NNN, TO SELL A STOCK TYPE -NNN, WHERE NNN IS THE
								NUMBER OF SHARES.  A BROKERAGE FEE OF 1% WILL BE CHARGED
								ON ALL TRANSACTIONS.  NOTE THAT IF A STOCK'S VALUE DROPS
								TO ZERO IT MAY REBOUND TO A POSITIVE VALUE AGAIN.  YOU
								HAVE $10,000 TO INVEST.  USE INTEGERS FOR ALL YOUR INPUTS.
								(NOTE:  TO GET A 'FEEL' FOR THE MARKET RUN FOR AT LEAST
								10 DAYS)
								""");
                		System.out.print("-----GOOD LUCK!-----\n\n\n");
                	}
                	//Set game state to startup
                	gameState = GAME_STATE.STARTUP;
                    break;

                case STARTUP:
                	//Generate initial assets and random variables
                	stocks = initStocks();
                	assets = initAssets();
                	slope = generateSlope();
                	daysTrending = generateDaysTrending();
                	//Generate new randomized stock values based on the start values at init
                	generateNewStockValues();
                	//Display initial stock values
                	displayInitialStocks();
                	
                	displayExchangeAverage(false);
                	//Set game state to trade
                	gameState = GAME_STATE.TRADE;
                    break;

                case TRADE:
                	//Get values for individual asset trades
                	//The enterTrades method also verifies that requested trades don't exceed asset limits
					boolean validTrade;
					do {
						validTrade = enterTrades();
					}while (!validTrade);

                	//Set game state to end of day
                	gameState = GAME_STATE.END_OF_DAY;
                	break;

                case END_OF_DAY:
                	//Decrement daysTrending for current slope
                	daysTrending -= 1;
                	System.out.println("\n\n**********     END OF DAY'S TRADING     **********\n\n");
                	generateNewStockValues();
                	displayAssets();
                	displayExchangeAverage(true);
                	displayAssetSummary();
                	//Continue?
                	boolean playAgain = true;
					/*
					The original game in Basic had:
					DO YOU WISH TO CONTINUE (YES-TYPE Y1, NO-TYPE 0)?
					I have changed this to help game flow so that if, for example, you want to
					enter all 0s (zeros) to buying/selling stock it avoids accidentally also then
					entering a 0 (zero) to whether or not you want to continue playing.  I think
					this improves game play over all.
					*/
                	System.out.print("DO YOU WISH TO CONTINUE (YES-TYPE Y, NO-TYPE N)? ");
                	if ( !getYesOrNoInput() ) {
                		playAgain = false;
                	}
                	//Update slope and days trending
                	//Set game state to trade if continue = yes otherwise game state to game over
                	if (playAgain) {
                		if ( daysTrending == 0 ) {
                			daysTrending = generateDaysTrending();
                			slope = generateSlope();
                		}
                		gameState = GAME_STATE.TRADE;
                	}else {
                		gameState = GAME_STATE.END_GAME;
                	}
                    break;
                	
                case END_GAME:
                	System.out.println ("HOPE YOU HAD FUN!!");
                	gameState = GAME_STATE.GAME_OVER;
                	break;
                	
                case GAME_OVER:
                	break;
            }
        } while (gameState != GAME_STATE.GAME_OVER);
    }
    
    
    //Displays current asset summary at end of day
    private void displayAssetSummary() {
    	double stockAssets = 0.0d;
    	double totalAssets;
    	
    	for (Asset asset : assets) {
    		stockAssets += asset.getValue();
    	}
    	stockAssets = formatDouble(stockAssets);
    	totalAssets = formatDouble(stockAssets + cash);
		System.out.println("\nTOTAL STOCK ASSETS ARE   $ " + stockAssets + "\n"
				+ "TOTAL CASH ASSETS ARE    $ " + cash + "\n"
				+ "TOTAL ASSETS ARE         $ " + totalAssets +"\n\n");
	}


    //Used at Startup to display initial stock info to user
	private void displayInitialStocks() {
    	System.out.println("STOCK                       INITIALS      PRICE/SHARE");
    	for (Stock stock : stocks) {
    		System.out.println(stock.getName() + generateTabs(30 - stock.getName().length()) + stock.getShortName() + generateTabs(10) + stock.getCurrentPrice());
    	}
	}
	
    
	//Used to display assets throughout game play
    private void displayAssets() {
    	System.out.println("STOCK         PRICE/SHARE   HOLDINGS      VALUE         NET PRICE CHANGE");
    	for (Asset asset : assets) {
    		System.out.println(asset.getStock().getShortName() + generateTabs(12) 
    		+ asset.getStock().getCurrentPrice() + generateTabs(14 - Double.toString(asset.getStock().getCurrentPrice()).length())
    		+ asset.getQuantity() + generateTabs(16 - Double.toString(asset.getQuantity()).length()) 
    		+ (asset.getValue()) + generateTabs(13 - Double.toString(asset.getValue()).length()) 
    		+ asset.getStock().getPriceChange());
    	}
    }
    

    //Displays exchange averages at startup and end of day.  Bool withChange = true will display the average days change at end of day
    private void displayExchangeAverage(boolean withChange) {
    	double currentAverage = 0.0d;
    	double previousAverage = 0.0d;
    	for (Stock stock : stocks) {
    		currentAverage += stock.getCurrentPrice();
    		previousAverage += stock.getPreviousPrice();
    	}
    	currentAverage /= 5;
    	previousAverage /= 5;
    	currentAverage = formatDouble(currentAverage);
    	previousAverage = formatDouble(previousAverage);
    	double change = formatDouble(currentAverage - previousAverage);
    	System.out.println("\n\n");
    	if (!withChange) {
    		System.out.println("NEW YORK STOCK EXCHANGE AVERAGE:  " + currentAverage + "\n");
    	}else {
    		System.out.println("NEW YORK STOCK EXCHANGE AVERAGE:  " + currentAverage + " NET CHANGE  " + change + "\n");
    	}
    }
    
    
    //Present stocks to user and get input for trades (-nn, sell or +nn buy)
	private boolean enterTrades() {
		int i = 0;
		int[] assetChangeQuantity = new int[5];
		double[] assetChangeValue = new double[5];
		double totalAssetChange = 0.0;
		double totalAssetChangeWithFee;
		System.out.println("WHAT IS YOUR TRANSACTION IN");
		//Loop through stocks and get user input
		for (Stock stock : stocks) {
			System.out.print(stock.getShortName() + "? ");
			assetChangeQuantity[i] = getIntegerInput();
			i++;
		}
		//Check for over sold stock
		for (i = 0 ; i < 5 ; i++) {
			if (assetChangeQuantity[i] < 0) {
				if (-assetChangeQuantity[i] > assets.get(i).getQuantity()) {
					System.out.println("\nYOU HAVE OVERSOLD A STOCK; TRY AGAIN.");
					return false;
				}
			}	
		}
		//Check for over purchased assets
		for (i = 0 ; i < 5 ; i++) {
			assetChangeValue[i] = formatDouble(assetChangeQuantity[i] * assets.get(i).getStock().getCurrentPrice());
			totalAssetChange += assetChangeValue[i];			
		}
		/*
		totalAssetChangeWithFee is flawed in the original Basic program -- it deducts sales
		before calculating the fee.  At minimum it should at least include all purchases
		and should probably also include a fee on all sales.  Variables totalAssetPurchase and totalAssetSale are not
		defined in this code but could be so that:
		totalAssetChangeWithFee = (int)(100.0d * ((totalAssetPurchase * BROKERAGE_FEE) + (-totalAssetSale * BROKERAGE_FEE) + totalAssetChange) + 0.5) / 100.0d;
		But as originally implemented it is, 640 LET B5=INT(.01*T5*100+.5)/100, where T5=P5+S5 (purchase + sales):
		*/
		totalAssetChangeWithFee = formatDouble((totalAssetChange * BROKERAGE_FEE) + totalAssetChange);
		if (totalAssetChangeWithFee > cash) {
			System.out.println ("\nYOU HAVE USED $" + formatDouble(cash - totalAssetChangeWithFee) + " MORE THAN YOU HAVE.");
			return false;
		}
		//Commit asset changes
		for (i = 0 ; i < 5 ; i++) {
			assets.get(i).setQuantity(assets.get(i).getQuantity() + assetChangeQuantity[i]);
		}
		cash = formatDouble(cash - totalAssetChangeWithFee);
		//Return true for all trades valid
		return true;
	}


	//Initialize stock objects with starting values
    private ArrayList<Stock> initStocks(){
    	ArrayList<Stock> stocks = new ArrayList<>();
    	stocks.add(new Stock("INT. BALLISTIC MISSILES","IBM", 100.0d));
    	stocks.add(new Stock("RED CROSS OF AMERICA","RCA", 85.0d));
    	stocks.add(new Stock("LICHTENSTEIN, BUMRAP & JOKE","LBJ", 150.0d));
    	stocks.add(new Stock("AMERICAN BANKRUPT CO.","ABC", 140.0d));
    	stocks.add(new Stock("CENSURED BOOKS STORE","CBS", 110.0d));
    	return stocks;
    }
    
    
    //Initialize stock Asset holdings (cash is kept in the variable cash)
	private ArrayList<Asset> initAssets() {
		ArrayList<Asset> returnAssets = new ArrayList<>();
		for (Stock stock : stocks) {
			returnAssets.add(new Asset(stock, 0));
		}
		return returnAssets;
	}
	
    
	//Set stock trending slope
    private double generateSlope() {
    	double slope = (int)((Math.random()/10) * 100 + .5) / 100.0d;
    	//Set slope direction + or -
    	if (Math.random() > 0.5) {
    		slope *= -1;
    	}
    	return slope;
    }
    
    
    //Generate days trending to determine how long the current slope lasts
    private int generateDaysTrending() {
    	return (int) (4.99 * Math.random() + 1);
    }
    
    
    private void generateNewStockValues() {
    	//Set big change parameters
    	int bigChange;
    	boolean bigChangeUpFlag = false;
    	boolean bigChangeDownFlag = false;
		
		if (bigChangeUpDays == 0) {
			int[] bigChangeUpParams = generateBigChangeParams();
			bigChangeUpIndex = bigChangeUpParams[0];
			bigChangeUpDays = bigChangeUpParams[1]; 
			bigChangeUpFlag = true;
		}
		
		if (bigChangeDownDays == 0) {
			int[] bigChangeDownParams = generateBigChangeParams();
			bigChangeDownIndex = bigChangeDownParams[0];
			bigChangeDownDays = bigChangeDownParams[1];
			bigChangeDownFlag = true;
		}
    	//Loop through stocks to set changes
    	for (Stock stock : stocks) {
    		bigChange = 0;
    		//Check if stock is set for big change up
    		if (stocks.indexOf(stock) == bigChangeUpIndex && bigChangeUpFlag) {
    			bigChange += 10;
    			bigChangeUpFlag = false;
    		}
    		//Check if stock is set for big change down
    		if (stocks.indexOf(stock) == bigChangeDownIndex && bigChangeDownFlag) {
    			bigChange -= 10;
    			bigChangeDownFlag = false;
    		}
    		
    		double change = Math.random();
    		if (change <= 0.25) {
    			change = 0.25;
    		}else if (change <= 0.50) {
    			change = 0.50;
    		}else if (change <= 0.75) {
    			change = 0.75;
    		}else {
    			change = 0.00;
    		}
    		//Calculate total change for current stock
    		double totalChange = (int)(slope * stock.getCurrentPrice()) + change + (3 - 6*Math.random() + .5) + bigChange;
    		totalChange = formatDouble(totalChange);
    		stock.setPreviousPrice(stock.getCurrentPrice());
    		stock.setCurrentPrice(formatDouble(totalChange + stock.getCurrentPrice()));
    		stock.setPriceChange(totalChange);
    	}
    	bigChangeUpDays -= 1;
		bigChangeDownDays -= 1;
    }
    
    
    private int[] generateBigChangeParams() {
    	int[] bigChange = new int[2];
    	bigChange[0] = (int) (4.99 * Math.random());
    	bigChange[1] = (int) (4.99 * Math.random() + 1);
    	return bigChange;
    }
    
    
    private String generateTabs(int numSpaces) {
    	char[] spaces = new char[numSpaces];
    	for (int i = 0 ; i < numSpaces; i++) {
    		Arrays.fill(spaces, ' ');
    	}
    	return new String(spaces);
    }

	private double formatDouble(double number){
		return ((int)(100.0d * number + 0.5) / 100.0d);
	}

    
    private int getIntegerInput() {
    	int i = 0;
    	boolean validInput = false;
    	do {
    		String kbInput = kbScanner.next();
    		try {
    			i = Integer.parseInt(kbInput);
    			validInput = true;
    		} catch (NumberFormatException e) {
    			System.out.println ("!NUMBER EXPECTED - RETRY INPUT LINE");
    			System.out.print("?");
    		}
    	} while (!validInput);
    	return i;
    }

	private boolean getYesOrNoInput() {
		boolean validInput;
		boolean yesOrNoInput = true;
		do {
			String kbInput = kbScanner.next();
			if (kbInput.toLowerCase().startsWith("y")){
				validInput = true;
			}else if (kbInput.toLowerCase().startsWith("n")){
				validInput = true;
				yesOrNoInput = false;
			}else {
				System.out.println ("!EXPECTED Y OR N - RETRY INPUT LINE");
				System.out.print("?");
				validInput = false;
			}
		} while (!validInput);
		return yesOrNoInput;
	}

}
