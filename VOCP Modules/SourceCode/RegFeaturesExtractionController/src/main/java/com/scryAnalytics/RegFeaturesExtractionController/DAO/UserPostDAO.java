package com.scryAnalytics.RegFeaturesExtractionController.DAO;

public class UserPostDAO {

	private String topic;
	private String profile_url;
	private String forum;
	private String member_id;
	private String location;
	private String post_url;
	private String join_date;
	private String page_number;
	private String user_sig;
	private String post_number;
	private String post_count;
	private String message;
	private String is_post;
	private String website;
	private String member_name;
	private String post_time;
	private String system_id;
	private String ailment_domain = "";
	private String is_processed = "0";
	private String post_id = "";

	public String getIs_post() {
		return is_post;
	}

	public void setIs_post(String is_post) {
		this.is_post = is_post;
	}

	public String getPost_id() {
		return post_id;
	}

	public void setPost_id(String post_id) {
		this.post_id = post_id;
	}

	public String getSystem_id() {
		return system_id;
	}

	public void setSystem_id(String system_id) {
		this.system_id = system_id;
	}

	public String getAilment_domain() {
		return ailment_domain;
	}

	public void setAilment_domain(String ailment_domain) {
		this.ailment_domain = ailment_domain;
	}

	public String getIs_processed() {
		return is_processed;
	}

	public void setIs_processed(String is_processed) {
		this.is_processed = is_processed;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getUser_sig() {
		return user_sig;
	}

	public void setUser_sig(String user_sig) {
		this.user_sig = user_sig;
	}

	public String getProfile_url() {
		return profile_url;
	}

	public void setProfile_url(String profile_url) {
		this.profile_url = profile_url;
	}

	public String getForum() {
		return forum;
	}

	public void setForum(String forum) {
		this.forum = forum;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPost_url() {
		return post_url;
	}

	public void setPost_url(String post_url) {
		this.post_url = post_url;
	}

	public String getJoin_date() {
		return join_date;
	}

	public void setJoin_date(String join_date) {
		this.join_date = join_date;
	}

	public String getPage_number() {
		return page_number;
	}

	public void setPage_number(String page_number) {
		this.page_number = page_number;
	}

	public String getPost_number() {
		return post_number;
	}

	public void setPost_number(String post_number) {
		this.post_number = post_number;
	}

	public String getPost_count() {
		return post_count;
	}

	public void setPost_count(String post_count) {
		this.post_count = post_count;
	}

	public String getMember_name() {
		return member_name;
	}

	public String getMember_id() {
		return member_id;
	}

	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public void setMember_name(String member_name) {
		this.member_name = member_name;
	}

	public String getPost_time() {
		return post_time;
	}

	public void setPost_time(String post_time) {
		this.post_time = post_time;
	}

	@Override
	public String toString() {
		return "UserPostDAO [topic=" + topic + ", profile_url=" + profile_url
				+ ", forum=" + forum + ", member_id=" + member_id
				+ ", location=" + location + ", post_url=" + post_url
				+ ", join_date=" + join_date + ", page_number=" + page_number
				+ ", user_sig=" + user_sig + ", post_number=" + post_number
				+ ", post_count=" + post_count + ", message=" + message
				+ ", is_post=" + is_post + ", website=" + website
				+ ", member_name=" + member_name + ", post_time=" + post_time
				+ ", system_id=" + system_id + ", ailment_domain="
				+ ailment_domain + ", is_processed=" + is_processed
				+ ", post_id=" + post_id + "]";
	}

}
