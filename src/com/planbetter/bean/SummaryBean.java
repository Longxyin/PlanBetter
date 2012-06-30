package com.planbetter.bean;

public class SummaryBean {

	public static String TABLE_NAME = "summary";
	public static String ID = "summray_id";
	public static String SCORE_RANK = "score_rank";
	public static String MOOD = "mood";
	public static String DATE = "summary_date";

	private int summaryId;
	private int scoreRank;
	private String mood;
	private String summaryDate;
	
	public SummaryBean() {
		
	}

	public SummaryBean(int summaryId, int scoreRank, String mood,
			String summaryDate) {
		this.summaryId = summaryId;
		this.scoreRank = scoreRank;
		this.mood = mood;
		this.summaryDate = summaryDate;
	}

	public int getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(int summaryId) {
		this.summaryId = summaryId;
	}

	public int getScoreRank() {
		return scoreRank;
	}

	public void setScoreRank(int scoreRank) {
		this.scoreRank = scoreRank;
	}

	public String getMood() {
		return mood;
	}

	public void setMood(String mood) {
		this.mood = mood;
	}

	public String getSummaryDate() {
		return summaryDate;
	}

	public void setSummaryDate(String summaryDate) {
		this.summaryDate = summaryDate;
	}

}
