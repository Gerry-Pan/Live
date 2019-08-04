package cn.com.pan.live.service;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.pan.live.entity.mini.Mini;
import cn.com.pan.live.entity.mini.MiniInvokeHandler;
import cn.com.pan.live.util.algorithm.PKCS7Encoder;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;

@Service
public class LiveService {

	protected final Logger log = LogManager.getLogger(getClass());

	@Autowired
	protected WebClient webClient;
	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected Properties miniProperties;
	@Autowired
	protected TopicProcessor<String> messageProcessor;
	@Autowired
	protected ReactiveMongoTemplate reactiveMongoTemplate;
	@Autowired
	protected ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate;

	private final ConcurrentMap<String, MiniInvokeHandler> handlerRepository = new ConcurrentHashMap<String, MiniInvokeHandler>();

	public Mono<JSONObject> switchIfEmpty(Integer code, String message) {
		JSONObject result = new JSONObject();
		result.put("code", code);
		result.put("message", message);
		return Mono.just(result);
	}

	public MiniInvokeHandler getMiniInvokeHandler(Mini mini) {
		assert mini != null : "Mini must not be null.";

		MiniInvokeHandler miniInvokeHandler = null;

		if (handlerRepository.containsKey(mini.getId())) {
			miniInvokeHandler = handlerRepository.get(mini.getId());
			miniInvokeHandler.setMini(mini);
		} else {
			miniInvokeHandler = new MiniInvokeHandler(mini, miniProperties, webClient, reactiveRedisTemplate,
					reactiveMongoTemplate);
		}

		return miniInvokeHandler;
	}

	protected String decrypt(String encryptedData, String iv, String sessionkey) {
		try {
			byte[] aesKey = Base64.decodeBase64(sessionkey);
			byte[] ivKey = Base64.decodeBase64(iv);

			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec key_spec = new SecretKeySpec(aesKey, "AES");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(Arrays.copyOfRange(ivKey, 0, 16));
			cipher.init(Cipher.DECRYPT_MODE, key_spec, ivParameterSpec);

			byte[] encrypted = Base64.decodeBase64(encryptedData);
			byte[] original = cipher.doFinal(encrypted);

			byte[] bytes = PKCS7Encoder.decode(original);

			return new String(bytes, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	protected <T> T resolveEntity(JSONObject requestBody, T entity) {
		Class<?> clazz = entity.getClass();
		Set<String> keySet = requestBody.keySet();
		BeanWrapper bw = new BeanWrapperImpl(entity);

		for (String key : keySet) {
			Class<?> propertyType = bw.getPropertyType(key);
			if (propertyType != null) {
				if (isInterface(propertyType, "java.util.Collection")) {
					Field f = ReflectionUtils.findField(clazz, key);
					Type fc = f.getGenericType();

					if (fc instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType) fc;
						Type[] actualTypes = pt.getActualTypeArguments();

						if (actualTypes != null && actualTypes.length > 0) {
							Class<?> c = (Class<?>) actualTypes[0];

							JSONArray a = requestBody.getJSONArray(key);

							if (a == null || a.size() == 0) {
								continue;
							}

							Object value = Array.newInstance(c, a.size());
							for (int i = 0; i < a.size(); i++) {
								JSONObject o = a.getJSONObject(i);
								Object v = o.toJavaObject(c);
								Array.set(value, i, v);
							}

							bw.setPropertyValue(key, value);
						}
					}
				} else {
					Object value = requestBody.getObject(key, propertyType);
					bw.setPropertyValue(key, value);
				}
			}
		}

		return entity;
	}

	protected boolean isInterface(Class<?> c, String szInterface) {
		Class<?>[] face = c.getInterfaces();
		for (int i = 0, j = face.length; i < j; i++) {
			if (face[i].getName().equals(szInterface)) {
				return true;
			} else {
				Class<?>[] face1 = face[i].getInterfaces();
				for (int x = 0; x < face1.length; x++) {
					if (face1[x].getName().equals(szInterface)) {
						return true;
					} else if (isInterface(face1[x], szInterface)) {
						return true;
					}
				}
			}
		}
		if (null != c.getSuperclass()) {
			return isInterface(c.getSuperclass(), szInterface);
		}
		return false;
	}

}
