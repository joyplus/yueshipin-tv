package com.joyplus.tv.entity;

/**
 * 电影显示单个item中所需信息
 * @author Administrator
 *
 */
public class MovieItemData {
	
	private String moviePicUrl;
	private String movieName;
	private String movieScore;
	public String getMoviePicUrl() {
		return moviePicUrl;
	}
	public void setMoviePicUrl(String moviePicUrl) {
		this.moviePicUrl = moviePicUrl;
	}
	public String getMovieName() {
		return movieName;
	}
	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}
	public String getMovieScore() {
		return movieScore;
	}
	public void setMovieScore(String movieScore) {
		this.movieScore = movieScore;
	}
	
	

}
