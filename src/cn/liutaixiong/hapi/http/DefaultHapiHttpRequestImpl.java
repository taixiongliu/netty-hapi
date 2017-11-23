package cn.liutaixiong.hapi.http;

import java.util.Map;

/**
 * <b>Parse request uri and parameter only normal</b>
 * @author taixiong.liu
 *
 */
public class DefaultHapiHttpRequestImpl extends BaseHapiHttpRequestImpl{

	@Override
	public Map<String, String> parseParameter(String content) throws HttpUrlErrorException {
		// TODO Auto-generated method stub
		return getParameterMapByUrlParameter(content, true);
	}
}
