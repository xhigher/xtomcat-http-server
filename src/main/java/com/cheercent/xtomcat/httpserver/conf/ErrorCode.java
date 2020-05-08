package com.cheercent.xtomcat.httpserver.conf;


public interface ErrorCode {
	int OK = 0;
	int NOK = 1;
	
	int INTERNAL_ERROR = 4000;
	int REQEUST_ERROR = 4001;
	int METHOD_ERROR = 4002;
	int PARAMETER_ERROR = 4003;
	int VALIDATION_ERROR  = 4004;
	int SERVICE_BUSY = 4005;
	int FORBIDDEN  = 4006;
	int TOKEN_INVALID  = 4007;
	int INFO_NULL = 4008;

	int SESSION_INVALID = 4101;
	int USERNAME_ERROR = 4102;
	int PASSWORD_ERROR = 4103;
	int PASSWORD_UNSET = 4104;
	
	int ACCOUNT_NULL = 4107;
	int ACCOUNT_EXISTED = 4108;
	int ACCOUNT_BLOCKED = 4109;
	int ACCOUNT_UNBOUND = 4110;

}