package cn.com.ecloud.live.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component("globalProperties")
public class GlobalProperties {

	@Getter
	@Value(value = "${rtmpPath}")
	private String rtmpPath;

}
