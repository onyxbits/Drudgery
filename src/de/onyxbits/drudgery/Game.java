package de.onyxbits.drudgery;

import java.util.Random;

import android.content.res.Resources;

/**
 * Implements the "Drudge Quest" game logic and state.
 * 
 * @author patrick
 * 
 */
class Game {

	/**
	 * Score per round
	 */
	public static final int SIMPLESCORE = 1;

	/**
	 * Score per round if accepting alternating tokens
	 */
	public static final int RHYTHMSCORE = 3;

	/**
	 * Awarded when being in the rhythm long enough.
	 */
	public static final int BONUSSCORE = 4;

	/**
	 * Get a bonus round if staying in the rhythm this long
	 */
	public static final int BONUSROUND = 5;

	/**
	 * Round type: a normal, in the rhythm round
	 */
	public static final int TYPENORMAL = 0;

	/**
	 * Round type: missed the rhythm
	 */
	public static final int TYPESETBACK = 1;

	/**
	 * Round type: bonus round
	 */
	public static final int TYPEBONUS = 2;

	/**
	 * Current experience points.
	 */
	protected int score;

	/**
	 * Round counter
	 */
	protected int round;

	/**
	 * Number of rounds with alternating choices.
	 */
	protected int runLength;

	/**
	 * Previous choice
	 */
	protected boolean lastChoice;

	/**
	 * Current choice.
	 */
	protected boolean nextChoice;

	/**
	 * Just a cache, not used in here.
	 */
	protected int highscore;

	/**
	 * The title that is awarded to the player after completing the game.
	 */
	protected String title;

	/**
	 * Determines message and score of this round.
	 */
	protected int roundtype;

	/**
	 * How often the player failed to alternate choices in this game.
	 */
	protected int setbackCounter;

	/**
	 * Stores all the possible room descriptions. First dimension is indexed by
	 * type, second by eventIndex.
	 */
	private String[][] events;
	private int eventIndex;

	private Random rng;
	private Resources res;

	/**
	 * Construct the game state for today.
	 * 
	 * @param highscore
	 *          highest game score ever
	 * @param res
	 *          resource object from which to lookup strings.
	 */
	public Game(int highscore, Resources res) {
		rng = new Random(System.currentTimeMillis());
		this.res = res;
		String[] titles = res.getStringArray(R.array.titles);
		title = titles[rng.nextInt(titles.length)];
		this.highscore = highscore;
		events = new String[3][0];
		events[TYPENORMAL] = res.getStringArray(R.array.normalevents);
		events[TYPESETBACK] = res.getStringArray(R.array.setbackevents);
		events[TYPEBONUS] = res.getStringArray(R.array.bonusevents);
	}

	/**
	 * Build the message to show while on the job
	 * 
	 * @return a human readable description of the game state.
	 */
	protected String buildQuestMessage() {
		String side = res.getString(R.string.left);
		if (nextChoice) {
			side = res.getString(R.string.right);
		}
		return res.getString(R.string.questmessage,
				events[roundtype][eventIndex], side,
				res.getQuantityString(R.plurals.times, runLength, runLength),
				res.getQuantityString(R.plurals.times, setbackCounter, setbackCounter),
				score);
	}

	/**
	 * Build the message to show while off the job.
	 * 
	 * @return a human readable description of the game state.
	 */
	protected String buildClosingTimeMessage() {
		return res.getString(R.string.closingtimemessage, score, highscore, title);
	}

	/**
	 * End the current round, evaluate the player's choice and set the stage for
	 * the new round.
	 */
	protected void newRound() {
		// First evaluate
		if (lastChoice != nextChoice) {
			if (runLength > 0) {
				if (runLength % BONUSROUND == 0) {
					score += BONUSSCORE;
					roundtype = TYPEBONUS;
				}
				else {
					score += RHYTHMSCORE;
					roundtype = TYPENORMAL;
				}
			}
			else {
				score += SIMPLESCORE;
				roundtype = TYPENORMAL;
			}
			runLength++;
		}
		else {
			score += SIMPLESCORE;
			roundtype = TYPESETBACK;
			runLength = 0;
			setbackCounter++;
		}

		// Set the stage
		lastChoice = nextChoice;
		nextChoice = rng.nextBoolean();
		eventIndex = rng.nextInt(events[roundtype].length);
		round++;
	}
}
