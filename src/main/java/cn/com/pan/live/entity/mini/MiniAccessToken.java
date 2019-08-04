package cn.com.pan.live.entity.mini;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;

@Document(collection = "MiniAccessToken")
public class MiniAccessToken implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 小程序appid
	 */
	@Id
	@Getter
	private String id;

	@Getter
	private String accessToken;

	@Getter
	@Indexed(expireAfterSeconds = 7100)
	private Date createdAt;

	public MiniAccessToken setId(String id) {
		this.id = id;
		return this;
	}

	public MiniAccessToken setAccessToken(String accessToken) {
		this.accessToken = accessToken;
		return this;
	}

	public MiniAccessToken setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
		return this;
	}

}
