package com.orange.game.api.exchange;

import javax.servlet.http.HttpServletRequest;
import com.orange.game.api.service.CommonGameService;

public class ExchangeAppService extends CommonGameService {

	HttpServletRequest request;
	
	@Override
	public boolean setDataFromRequest(HttpServletRequest request) {
		this.request = request;
		return false;
	}

	@Override
	public void handleData() {
		
	}

}
