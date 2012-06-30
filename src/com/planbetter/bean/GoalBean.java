package com.planbetter.bean;

public class GoalBean {

	public static String TABLE_NAME = "goal";
	public static String ID = "goal_id";
	public static String DATE = "goal_date";
	public static String GOAL_CONTENT = "goal_content";
	public static String GOAL_FLAG = "goal_flag";

	private int goalId;
	private String goalDate;
	private String goalCotent;
	private int goalFlag;

	public int getGoalId() {
		return goalId;
	}

	public void setGoalId(int goalId) {
		this.goalId = goalId;
	}

	public String getGoalDate() {
		return goalDate;
	}

	public void setGoalDate(String goalDate) {
		this.goalDate = goalDate;
	}

	public String getGoalCotent() {
		return goalCotent;
	}

	public void setGoalCotent(String goalCotent) {
		this.goalCotent = goalCotent;
	}

	public int getGoalFlag() {
		return goalFlag;
	}

	public void setGoalFlag(int goalFlag) {
		this.goalFlag = goalFlag;
	}

	public GoalBean(int goalId, String goalDate, String goalCotent, int goalFlag) {
		this.goalId = goalId;
		this.goalDate = goalDate;
		this.goalCotent = goalCotent;
		this.goalFlag = goalFlag;
	}

	public GoalBean() {

	}

}
