package edu.washington.cs.mystatus;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Button;
import android.widget.Spinner;

/**
 * GoalsActivity provides a UI for setting new goals and seeing active goals.
 * 
 * @author eechien@cs.washington.edu
 */

public class GoalsActivity extends Activity {

	private Spinner newGoal, removeGoal;
	private Button newGoalButton, removeGoalButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goals);
		
		
	}
}
