package edu.washington.cs.mystatus;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

/**
 * GoalsActivity provides a UI for setting new goals and seeing active goals.
 * 
 * @author eechien@cs.washington.edu
 */

public class GoalsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goals);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_goals, menu);
		return true;
	}

}
