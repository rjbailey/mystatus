package edu.washington.cs.mystatus;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

/**
 * GoalsActivity provides a UI for setting new goals, seeing active goals, and removing
 * active goals.
 * Goals are prepopulated
 * 
 * @author eechien@cs.washington.edu
 */

public class GoalsActivity extends Activity {

	private Spinner newGoal, removeGoal;
	private Button newGoalButton, removeGoalButton;
	private ArrayAdapter<String> goalPopulatorForRemove;
	private ArrayAdapter<String> goalPopulatorForList;
	private List<String> setGoals;
	private ListView setGoalsView;
	//private Gson gson;
	//private String[] goalsList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_goals);
		
		setGlobalVariables();
		
		//GsonBuilder gsonb = new GsonBuilder();
		//gson = gsonb.create();
		//goalsList = gson.fromJson(goalsPrefs, String[].class);
		//setGoals = gson.fromJson(goalsPrefs, List<String>.class);
		//setGoals = new ArrayList<String>(Arrays.asList(goalsList));
		makeArrayAdapters();
		
		addListenersOnButtons();
	}

	private void addListenersOnButtons() {
		
		newGoalButton.setOnClickListener(new OnClickListener() {
			@Override
			// When "Select" is clicked, add the selected goal to the list of Set Goals
			// This will populate the ListView and the dropdown for Removing goals.
			public void onClick(View v) {
				//SharedPreferences settings = getPreferences(MODE_PRIVATE);
				//Editor e = settings.edit();
				
				String newGoalItem = String.valueOf(newGoal.getSelectedItem());
				// Check that the item is not already a Set Goal to avoid duplicates
				if (!setGoals.contains(newGoalItem)) {
					setGoals.add(newGoalItem);
					removeGoal.setAdapter(goalPopulatorForRemove);
					setGoalsView.setAdapter(goalPopulatorForList);
					//String value = gson.toJson(goalsList);
					//e.putString("set_goals", value);
				}
				//e.commit();
			}
		});
		
		removeGoalButton.setOnClickListener(new OnClickListener() {
			@Override
			// When "Select" is clicked, remove the selected goal from the list of Set Goals
			// This will remove items from the ListView and the dropdown for Removing goals.
			public void onClick(View v) {
				String goalItem = String.valueOf(removeGoal.getSelectedItem());
				// Check that the item is actually already a Set Goal
				if (setGoals.contains(goalItem)) {
					setGoals.remove(goalItem);
					removeGoal.setAdapter(goalPopulatorForRemove);
					setGoalsView.setAdapter(goalPopulatorForList);
				}
			}
		});
	}
	
	private void setGlobalVariables() {
		setGoalsView = (ListView) findViewById(R.id.set_goals_list);
		newGoalButton = (Button) findViewById(R.id.choose_new_goal);
		removeGoalButton = (Button) findViewById(R.id.remove_goal_choice);
		newGoal = (Spinner) findViewById(R.id.goal_options);
		removeGoal = (Spinner) findViewById(R.id.remove_goals_options);
		setGoals = new ArrayList<String>();
	}
	
	private void makeArrayAdapters() {
		// An adapter for the remove dropdown and the list of current goals
		goalPopulatorForRemove = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, setGoals);
		goalPopulatorForRemove.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		goalPopulatorForList = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, setGoals);
	}
}
