# xtomcat-http-server
基于tomcat-embed web容器构建的微服务应用框架

简介
---
  基于tomcat-embed web容器构建的微服务应用框架，resful API风格，json数据格式交互，核心逻辑简洁易懂，定制灵活；mysql，redis，mongodb，elasticsearch等即开即用，资源管理完全托管。
  业务逻辑层只需专注于业务流程实现，数据层无缝支持多数据源；Servlet容器采用内嵌Tomcat，快速启动，方便本地测试和服务上线，部署简单，通过MAVEN工具能够打出各个环境（根据指定配置文件）安装包；


启动服务

```java
	final XServer server = new XServer(properties);

	Runtime.getRuntime().addShutdownHook(new Thread(){
		@Override
		public void run() {
			server.stop();
		}
	});

	server.start();
```
 
业务层
```java
	//注解：接口名，版本（扩展同名接口），请求方式，必要参数，限定ip等
	@XLogicConfig(name = "login", version = 2, method = ActionMethod.POST, requiredParameters = {
	        DataKey.USERNAME,
	        DataKey.PASSWORD
	})
	public class Login extends XLogic {

		//是否校验用户登录态
	    @Override
	    protected boolean requireSession() {
	        return false;
	    }
	    
	    //本地参数变量
	    private String username;
	    private String password;


		//校验参数等
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


		//执行业务流程
		@Override
		protected String execute() {
			JSONObject resultData = new JSONObject();
			resultData.put(DataKey.USERNAME, username);
			resultData.put(DataKey.PASSWORD, password);
			return XLogicResult.success(resultData);
		}
	}
```

数据层

```java
 	//数据库

	public abstract class BusinessDatabase extends XModel {

		//指定配置数据源
		public static final String dataSourceName = "business";
		
		@Override
		protected String getDataSourceName() {
			return dataSourceName;
		}
		
	}

 	//业务表
 	//继承指定数据源的基类
 	public class UserInfoModel extends BusinessDatabase {

	 	//指定用户表名
		@Override
		protected String tableName() {
			return "user_info";
		}

		//获取用户信息
		public JSONObject getInfo(String userid){
			return this.prepare().addWhere("userid",userid).find();
		}
		
		//更新用户昵称
		public boolean updateInfo(String userid, String nickname){
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("nickname", nickname);
			values.put("updatetime", CommonUtils.getCurrentYMDHMS());
			return this.prepare().set(values).addWhere("userid", userid).update();
		}

	}
```






