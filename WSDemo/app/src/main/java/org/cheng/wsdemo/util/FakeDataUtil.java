package org.cheng.wsdemo.util;

import org.cheng.wsdemo.http.HttpUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FakeDataUtil {
    public static String SenderUid = "1000002";
    public static String GroupName="NewGroup";
    public static int MaxGroupNum=2000;

    public static final String HttpAddress = "http://"+WebSocketUtil.IP+":8899?action=";
    public static final String LoginHttpAddress = HttpAddress + "login";
    public static final String EmailHttpAddress = HttpAddress + "register";
    public static final String AuthHttpAddress = HttpAddress + "auth&type=register";
    public static final String FindFriendsAddress = HttpAddress + "search&type=user";
    public static final String FindGroupInfo=HttpAddress+"search&type=group&detail";
    public static final String FindUserInfo=HttpAddress+"search&type=user";
    public static final String UpdateUserInfo=HttpAddress+"update";


    public static  String[] removeArrayEmptyTextBackNewArray(String[] strArray) {
        List<String> strList    = Arrays.asList(strArray);
        List<String> strListNew =new ArrayList<>();
        for (int i = 0; i <strList.size(); i++) {
            if (strList.get(i)!=null&&!strList.get(i).equals("")){
                strListNew.add(strList.get(i));
            }
        }
        String[] strNewArray = strListNew.toArray(new String[strListNew.size()]);
        return   strNewArray;
    }
}
