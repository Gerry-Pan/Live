package cn.com.pan.live.entity.mini;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;

@Document(collection = "MiniUserSessionKey")
public class MiniUserSessionKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Getter
	private String id;

	@Getter
	private String sessionKey;

	public MiniUserSessionKey setId(String id) {
		this.id = id;
		return this;
	}

	public MiniUserSessionKey setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
		return this;
	}

}
