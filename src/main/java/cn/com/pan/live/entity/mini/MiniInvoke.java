package cn.com.pan.live.entity.mini;

import java.io.Serializable;

import org.springframework.http.HttpMethod;

public class MiniInvoke implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8730093968741883845L;

	private String id;

	private String contentType;

	private HttpMethod method;

	private String url;

	public String getId() {
		return id;
	}

	public MiniInvoke setId(String id) {
		this.id = id;
		return this;
	}

	public String getContentType() {
		return contentType;
	}

	public MiniInvoke setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public MiniInvoke setUrl(String url) {
		this.url = url;
		return this;
	}

}
