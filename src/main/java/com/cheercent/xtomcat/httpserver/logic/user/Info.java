package com.cheercent.xtomcat.httpserver.logic.user;

import com.alibaba.fastjson.JSONObject;
import com.cheercent.xtomcat.httpserver.base.XLogic;
import com.cheercent.xtomcat.httpserver.base.XLogicConfig;
import com.cheercent.xtomcat.httpserver.base.XLogicResult;
import com.cheercent.xtomcat.httpserver.conf.DataKey;

@XLogicConfig(name = "info")
public final class Info extends XLogic {


	@Override
	protected String prepare() {
		
		return null;
	}

	@Override
	protected String execute() {
		JSONObject resultData = new JSONObject();
		resultData.put(DataKey.PEERID, logicSession.getPeerid());
		return XLogicResult.success(resultData);
	}
}
