package com.jayway.android.robotium.core.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Instrumentation;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class contains various search methods. Examples are: searchForEditTextWithTimeout(),
 * searchForTextWithTimeout(), searchForButtonWithTimeout().
 * 
 * @author Renas Reda, renas.reda@jayway.com
 * 
 */

public class Searcher {
	
	private final ViewFetcher viewFetcher;
	private final Scroller scroller;
	private final Instrumentation inst;
    private final Sleeper sleeper;
	private final int TIMEOUT = 5000;
	private final String LOG_TAG = "Robotium";
	
    /**
     * Constructs this object.
     *
     * @param viewFetcher the {@code ViewFetcher} instance.
     * @param scroller the {@code Scroller} instance.
     * @param inst the {@code Instrumentation} instance.
     * @param sleeper the {@code Sleeper} instance.
     */
    public Searcher(ViewFetcher viewFetcher, Scroller scroller, Instrumentation inst, Sleeper sleeper) {
        this.viewFetcher = viewFetcher;
        this.scroller = scroller;
        this.inst = inst;
        this.sleeper = sleeper;
    }
	




	
	/**
	 * Searches for a text string in the {@link EditText}s located in the current
	 * {@code Activity}. Will automatically scroll when needed.
	 *
	 * @param regex the text to search for. The parameter <strong>will</strong> be interpreted as a regular expression.
	 * @return {@code true} if an {@code EditText} with the given text is found or {@code false} if it is not found
	 *
	 */
    
    public boolean searchForEditTextWithTimeout(String regex) {
        long now = System.currentTimeMillis();
        final long endTime = now + TIMEOUT;

		boolean shouldContinue = true;
		while (shouldContinue) {
			sleeper.sleep();
			shouldContinue = !searchFor(EditText.class, regex, 1, true) && now < endTime;
			sleeper.sleep();
			now = System.currentTimeMillis();
        }

		sleeper.sleep();

		//TODO: why does it call searchFor again, and not just evaluate whether it has found a match?
		//TODO: if this is not necessary, we can simply delegate to searchWithTimeoutFor(EditText.class, ....) and refactor away this entire method
		return searchFor(EditText.class, regex, 1, true);
	}


	/**
	 * Searches for a {@code View} with the given regex string and returns {@code true} if the
	 * searched {@code Button} is found a given number of times. Will automatically scroll when needed.
	 *
	 * @param viewClass what kind of {@code View} to search for, e.g. {@code Button.class} or {@code TextView.class}
	 * @param regex the text to search for. The parameter <strong>will</strong> be interpreted as a regular expression.
	 * @param matches the number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found
	 * @param scroll whether scrolling should be performed
	 * @return {@code true} if a {@code View} of the specified class with the given text is found a given number of
	 * times, and {@code false} if it is not found
	 *
	 */
	public boolean searchWithTimeoutFor(Class<? extends TextView> viewClass, String regex, int matches, boolean scroll) {
		final long endTime = System.currentTimeMillis() + TIMEOUT;

		while (System.currentTimeMillis() < endTime) {
			sleeper.sleep();
			final boolean foundAnyMatchingView = searchFor(viewClass, regex, matches, scroll);
			if (foundAnyMatchingView){
				return true;
			}
		}

		return false;
	}


	/**
	 * Searches for a {@code View} with the given regex string and returns {@code true} if the
	 * searched {@code View} is found a given number of times
	 *
	 * @param viewClass what kind of {@code View} to search for, e.g. {@code Button.class} or {@code TextView.class}
	 * @param regex the text to search for. The parameter <strong>will</strong> be interpreted as a regular expression.
	 * @param expectedNumberOfMatches the number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found.
	 * @param scroll whether scrolling should be performed
	 * @return {@code true} if a view of the specified class with the given text is found a given number of times.
	 * {@code false} if it is not found.
	 *
	 */
	public <T extends TextView> boolean searchFor(Class<T> viewClass, String regex, int expectedNumberOfMatches, boolean scroll) {
		inst.waitForIdleSync();

		if(expectedNumberOfMatches == 0) {
			expectedNumberOfMatches = 1;
		}
		int matchesFound = 0;

		List<T> views = viewFetcher.getCurrentViews(viewClass);

		final Pattern pattern = Pattern.compile(regex);
		for(T view : views){
			final Matcher matcher = pattern.matcher(view.getText().toString());
			if (matcher.find()){
				matchesFound++;
			}
			if (matchesFound == expectedNumberOfMatches) {
				return true;
			}
		}

		if (scroll && scroller.scroll(Scroller.Direction.DOWN)) {
			sleeper.sleep();
			return searchFor(viewClass, regex, expectedNumberOfMatches, scroll);
		} else {
			if (matchesFound > 0) {
				Log.d(LOG_TAG, " There are only " + matchesFound + " matches of " + regex);
			}
			return false;
		}
	}


}