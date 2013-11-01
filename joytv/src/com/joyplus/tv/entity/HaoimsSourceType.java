package com.joyplus.tv.entity;

import java.util.ArrayList;

public enum HaoimsSourceType {

	SOURCE_ALL			(0){
		public int getIndex() {
			return 0;
		}

		private ArrayList<ISourceData> allList= new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.allList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return this.allList;
		}
		
		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	},
	SOURCE_MOVIE		(1) {
		public int getIndex() {
			return 1;
		}

		private ArrayList<ISourceData> movieList= new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.movieList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return this.movieList;
		}
		
		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	},
	SOURCE_TVSERIES		(2) {
		
		public int getIndex() {
			return 2;
		}

		private ArrayList<ISourceData> tvseriesList= new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.tvseriesList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return this.tvseriesList;
		}
		
		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	},
	SOURCE_VARIETY 		(3) {
		public int getIndex() {
			return 3;
		}

		private ArrayList<ISourceData> varietyList= new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.varietyList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return this.varietyList;
		}
		
		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	},
	SOURCE_LIVE			(4) {
		public int getIndex() {
			return 4;
		}

		private ArrayList<ISourceData> liveList= new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.liveList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return this.liveList;
		}
		
		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	},
	SOURCE_ANIME		(5) {
		public int getIndex() {
			return 5;
		}

		private ArrayList<ISourceData> animList= new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.animList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return this.animList;
		}
		
		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	},
	SOURCE_MUSIC 		(6) {
		public int getIndex() {
			return 6;
		}

		private ArrayList<ISourceData> musicList= new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.musicList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return this.musicList;
		}
		
		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	},
	SOURCE_DOCUMENTARY	(7) {

		public int getIndex() {
			return 7;
		}

		private ArrayList<ISourceData> documentaryList= new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.documentaryList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return documentaryList;
		}
		
		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	},
	SOURCE_EDUCATION 	(8) {
		public int getIndex() {
			return 8;
		}
		
		private ArrayList<ISourceData> educationList= new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.educationList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return this.educationList;
		}

		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	},
	SOURCE_OTHERS		(20) {
		public int getIndex() {
			return 9;
		}
		
		private ArrayList<ISourceData> othersList = new ArrayList<ISourceData>();
		public void setSourceDataList(ArrayList<ISourceData> list) {
			this.othersList = list;
		}
		public ArrayList<ISourceData> getSourceDatalist() {
			return othersList;
		}
		
		private int pageIndex = 1;
		public void setPageIndex(int pageIndex) {
			this.pageIndex = pageIndex;
		}
		public int getPageIndex() {
			return this.pageIndex;
		}
		
		private boolean isCanCache = false;
		public void setCanCache(boolean isCanCache){
			this.isCanCache = isCanCache;
		}
		public boolean isCanCache() {
			return isCanCache;
		}
	};
	
	private int typeValue;

	private HaoimsSourceType(int _typeValue){
		this.typeValue = _typeValue;
	}
	
	public int toTypeValue(){
		return this.typeValue;
	}
	
	public static final int DEFAULT_PAGE_SIZE = 20;
	private static final String HAOIMS_BASE_URL 
				= "http://m.haoims.com/search.php?action=search";
	
	//page_num start 1
	public String getRequestURL4Page(String app_key,int page_num,int page_size){
		return HAOIMS_BASE_URL + "&app_key=" + app_key + "&type=" + this.typeValue +
				"&page_size=" + page_size + "&page_num=" + page_num;
	}
	
	public String getRequestURL4Page(int page_num,int page_size){
		return getRequestURL4Page("null", page_num, page_size);
	}
	
	public abstract int 						getIndex();
	public abstract void 						setSourceDataList(ArrayList<ISourceData> list);
	public abstract ArrayList<ISourceData> 	getSourceDatalist();
	public abstract void 						setPageIndex(int pageIndex);
	public abstract int 						getPageIndex();
	public abstract void						setCanCache(boolean isCanCache);
	public abstract boolean                   isCanCache();
	
}
