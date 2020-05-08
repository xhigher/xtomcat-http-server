package com.cheercent.xtomcat.httpserver.logic.common;

import com.alibaba.fastjson.JSONObject;
import com.cheercent.xtomcat.httpserver.base.XLogic;
import com.cheercent.xtomcat.httpserver.base.XLogicConfig;
import com.cheercent.xtomcat.httpserver.base.XLogicConfig.ActionMethod;
import com.cheercent.xtomcat.httpserver.base.XLogicResult;
import com.cheercent.xtomcat.httpserver.conf.DataKey;
import com.cheercent.xtomcat.httpserver.util.CommonUtils;

@XLogicConfig(name = "login", version = 2, method = ActionMethod.POST, requiredParameters = {
        DataKey.USERNAME,
        DataKey.PASSWORD
})
public class Login extends XLogic {

    @Override
    protected boolean requireSession() {
        return false;
    }
    
    private String username;
    private String password;

    @Override
    protected String prepare() {
    	username = this.getString(DataKey.USERNAME);
        if (!CommonUtils.checkPhoneNo(username)) {
            return XLogicResult.errorParameter("USERNAME_ERROR");
        }
        password = this.getString(DataKey.PASSWORD);
        if (password.length() != 32) {
            return XLogicResult.errorParameter("PASSWORD_ERROR");
        }
        return null;
    }

	@Override
	protected String execute() {
		JSONObject resultData = new JSONObject();
		resultData.put(DataKey.USERNAME, username);
		resultData.put(DataKey.PASSWORD, password);
		return XLogicResult.success(resultData);
	}
}
