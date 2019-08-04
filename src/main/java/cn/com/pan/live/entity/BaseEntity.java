package cn.com.pan.live.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unchecked")
@JsonIgnoreProperties(value = { "target" })
public abstract class BaseEntity<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	protected Integer isDelete = 0;

	@JsonIgnore
	protected Date createTime;

	public Date getCreateTime() {
		return createTime;
	}

	public T setCreateTime(Date createTime) {
		this.createTime = createTime;
		return (T) this;
	}

	public Integer getIsDelete() {
		return isDelete;
	}

	public T setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
		return (T) this;
	}

}
