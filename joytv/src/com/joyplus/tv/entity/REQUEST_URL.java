package com.joyplus.tv.entity;

import com.joyplus.tv.Constant;

public enum REQUEST_URL {

	JOYPLUS_URL {
		private String getIsShoucangUrl() {
			return Constant.BASE_URL + "program/is_favority";
		}
		private String getDetailServiceUrl() {
			return Constant.BASE_URL + "program/view";
		}
		private String getDingUrl() {
			return Constant.BASE_URL + "program/support";
		}
		private String getShoucangUrl() {
			return Constant.BASE_URL + "program/favority";
		}
		private String getCancelShoucangUrl() {
			return Constant.BASE_URL + "program/unfavority";
		}
		private String getYingPingUrl() {
			return null;
		}
		private String getRecommendMovieUrl() {
			return Constant.BASE_URL + "program/relatedVideos";
		}
		
		private String getGroupSeriesTv(){
			return null;
		}
		
		public String getURL4URL_Type(URL_TYPE urlType) {
			String url = null;
			switch (urlType) {
			case IS_SHOUCANG_URL:
				url = getIsShoucangUrl();
				break;
			case DETAIL_SERVICE_URL:
				url = getDetailServiceUrl();
				break;
			case DING_URL:
				url = getDingUrl();
				break;
			case SHOUCANG_URL:
				url = getShoucangUrl();
				break;
			case CANCEL_SHOUCANG_URL:
				url = getCancelShoucangUrl();
				break;
			case YINGPING_URL:
				url = getYingPingUrl();
				break;
			case RECOMMEND_MOVIE_URL:
				url = getRecommendMovieUrl();
				break;
			case GROUPSERIESTV:
				url = getGroupSeriesTv();
				break;
			default:
				break;
			}
			return url;
		}
	},HAOIMS {
		public String getURL4URL_Type(URL_TYPE urlType) {
			String url = null;
			switch (urlType) {
			case IS_SHOUCANG_URL:
				break;
			case DETAIL_SERVICE_URL:
				url = HSOIMS_SEARCH_URL;
				break;
			case DING_URL:
				break;
			case SHOUCANG_URL:
				break;
			case CANCEL_SHOUCANG_URL:
				break;
			case YINGPING_URL:
				break;
			case RECOMMEND_MOVIE_URL:
				break;
			case GROUPSERIESTV:
				break;
			default:
				break;
			}
			return url;
		}
	};
	
	public enum URL_TYPE{
		IS_SHOUCANG_URL			("initIsShoucangData"),
		DETAIL_SERVICE_URL		("initData"),
		DING_URL				("dingResult"),
		SHOUCANG_URL			("shoucangResult"),
		CANCEL_SHOUCANG_URL		("cancelshoucangResult"),
		YINGPING_URL			("initYingpingServiceData"),
		RECOMMEND_MOVIE_URL		("initRecommendMovieDate"),
		GROUPSERIESTV			("initGroupSeriesServiceData"),
		SOURCE_URL_PARSER		("initSourceUrlParserServiceData");
		
		private String interfaceName;
		private URL_TYPE(String _interfaceName){
			this.interfaceName = _interfaceName;
		}
		
		public String getInterfaceName(){
			return this.interfaceName;
		}
		
	}
	
	private static final String HAOIMS_BASE_URL = "http://m.haoims.com";
	private static final String HSOIMS_SEARCH_URL = HAOIMS_BASE_URL + "/search.php";
	
	public abstract String getURL4URL_Type(URL_TYPE urlType);
}
