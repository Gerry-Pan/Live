package cn.com.pan.live.entity.mini;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.com.pan.live.entity.BaseEntity;

/**
 * 小程序用户
 * 
 * @author Jerry
 *
 */
@Document(collection = "MiniUser")
public class MiniUser extends BaseEntity<MiniUser> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 昵称
	 */
	private String nickname;

	private String mobile;

	private Integer gender;

	private String city;

	private String province;

	private String country;

	private String avatarUrl;

	@DBRef(lazy = true)
	private Mini mini;

	public String getId() {
		return id;
	}

	public MiniUser setId(String id) {
		this.id = id;
		return this;
	}

	public String getNickname() {
		return nickname;
	}

	public MiniUser setNickname(String nickname) {
		this.nickname = nickname;
		return this;
	}

	public Integer getGender() {
		return gender;
	}

	public MiniUser setGender(Integer gender) {
		this.gender = gender;
		return this;
	}

	public String getProvince() {
		return province;
	}

	public MiniUser setProvince(String province) {
		this.province = province;
		return this;
	}

	public String getCountry() {
		return country;
	}

	public MiniUser setCountry(String country) {
		this.country = country;
		return this;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public MiniUser setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
		return this;
	}

	public Mini getMini() {
		return mini;
	}

	public MiniUser setMini(Mini mini) {
		this.mini = mini;
		return this;
	}

	public String getCity() {
		return city;
	}

	public MiniUser setCity(String city) {
		this.city = city;
		return this;
	}

	public String getMobile() {
		return mobile;
	}

	public MiniUser setMobile(String mobile) {
		this.mobile = mobile;
		return this;
	}

	public String getName() {
		return name;
	}

	public MiniUser setName(String name) {
		this.name = name;
		return this;
	}

}
