package org.cheng.wsdemo.util;

import org.cheng.wsdemo.http.HttpUtil;

public class FakeDataUtil {
    public static String SenderUid = "1000002";
    public static final String HttpAddress = "http://10.128.15.148:8899?action=";
    public static final String LoginHttpAddress = HttpAddress + "login";
    public static final String EmailHttpAddress = HttpAddress + "register";
    public static final String AuthHttpAddress = HttpAddress + "auth";
    public static final String FindFriendsAddress = HttpAddress + "search";
}
