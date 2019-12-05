package org.cheng.wsdemo.data;

import org.cheng.wsdemo.data.model.LoggedInUser;
import org.cheng.wsdemo.http.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication
            HttpUtil.postDataWithParame("http://chat.onesrc.cn",username,password, new okhttp3.Callback(){
                @Override
                public void onResponse(Call call, Response response) throws IOException{
                    if(response.isSuccessful())
                    {
                        String responseData = response.body().string();
                        System.out.println(responseData);
                        System.out.println("登陆成功+"+responseData+"!");
                    }
                }
                @Override
                public void onFailure(Call call,IOException e){
                    System.out.println("登陆失败！");
                    //异常处理
                }
            });
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            username,
                            username);
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
