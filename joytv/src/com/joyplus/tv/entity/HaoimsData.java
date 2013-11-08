package com.joyplus.tv.entity;

public class HaoimsData implements ISourceData,Cloneable{
	
	private String prod_id;
	private String prod_name;
	private String prod_type;
	private String prod_pic_url;
	private String big_prod_pic_url;
	private String prod_sumary;
	private String star;
	private String director;
	private String favority_num;
	private String support_num;
	private String publish_date;
	private String score;
	private String area;
	private String max_episode;
	private String cur_episode;
	private String duration;
	
	public HaoimsData clone(){
		// TODO Auto-generated method stub
		
		try {
			return (HaoimsData)super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String getProd_id() {
		return prod_id;
	}
	public void setProd_id(String prod_id) {
		this.prod_id = prod_id;
	}
	public String getProd_name() {
		return prod_name;
	}
	public void setProd_name(String prod_name) {
		this.prod_name = prod_name;
	}
	public String getProd_type() {
		return prod_type;
	}
	public void setProd_type(String prod_type) {
		this.prod_type = prod_type;
	}
	public String getProd_pic_url() {
		return prod_pic_url;
	}
	public void setProd_pic_url(String prod_pic_url) {
		this.prod_pic_url = prod_pic_url;
	}
	public String getBig_prod_pic_url() {
		return big_prod_pic_url;
	}
	public void setBig_prod_pic_url(String big_prod_pic_url) {
		this.big_prod_pic_url = big_prod_pic_url;
	}
	public String getProd_sumary() {
		return prod_sumary;
	}
	public void setProd_sumary(String prod_sumary) {
		this.prod_sumary = prod_sumary;
	}
	public String getStar() {
		return star;
	}
	public void setStar(String star) {
		this.star = star;
	}
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public String getFavority_num() {
		return favority_num;
	}
	public void setFavority_num(String favority_num) {
		this.favority_num = favority_num;
	}
	public String getSupport_num() {
		return support_num;
	}
	public void setSupport_num(String support_num) {
		this.support_num = support_num;
	}
	public String getPublish_date() {
		return publish_date;
	}
	public void setPublish_date(String publish_date) {
		this.publish_date = publish_date;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getMax_episode() {
		return max_episode;
	}
	public void setMax_episode(String max_episode) {
		this.max_episode = max_episode;
	}
	public String getCur_episode() {
		return cur_episode;
	}
	public void setCur_episode(String cur_episode) {
		this.cur_episode = cur_episode;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
}
