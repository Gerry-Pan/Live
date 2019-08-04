package cn.com.pan.live.entity.mini;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.com.pan.live.entity.BaseEntity;

/**
 * 小程序表，miniprogram
 * 
 * @author Jerry
 *
 */
@Document(collection = "Mini")
public class Mini extends BaseEntity<Mini> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 小程序appid
	 */
	@Id
	private String id;

	private String name;

	private String secret;

	private String originalId;

	public String getId() {
		return id;
	}

	public Mini setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Mini setName(String name) {
		this.name = name;
		return this;
	}

	public String getSecret() {
		return secret;
	}

	public Mini setSecret(String secret) {
		this.secret = secret;
		return this;
	}

	public String getOriginalId() {
		return originalId;
	}

	public Mini setOriginalId(String originalId) {
		this.originalId = originalId;
		return this;
	}

}
