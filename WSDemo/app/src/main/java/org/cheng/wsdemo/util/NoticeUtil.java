package org.cheng.wsdemo.util;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import org.cheng.wsdemo.ui.MessagesActivity;

public class NoticeUtil {
    public static final String NO_CONNECT = "当前没有网络连接,正在重连!";
    public static final String NOT_ALLOWED_EMP = "不允许为空!";
    public static final String ADD_FRIENDS="添加好友请求已经发送！";

    public static void ShowImportMsg(String string, final Context context)
    {
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }
}
