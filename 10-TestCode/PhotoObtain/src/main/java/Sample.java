import com.baidu.aip.ocr.AipOcr;
import org.json.JSONArray;
import org.json.JSONObject;


import java.util.*;

public class Sample {
    //设置APPID/AK/SK
    public static final String APP_ID = "23786733";
    public static final String API_KEY = "IEA7y1gdKmtac0Gs0Qp1QKGL";
    public static final String SECRET_KEY = "lzNfnylmnkYnV2rZeYGIj2oQWdI0nEZX";

    public static void main(String[] args) {
        // 初始化一个AipOcr
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");

        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("detect_direction", "true");
        options.put("probability", "true");
        // 参数为本地图片路径
        String path = "H:\\FloorMeterTakePhoto\\ChaoBiao\\202102\\0AX8i38PHc.Jpg";
        JSONObject res = client.accurateGeneral(path, options);

        Iterator it = res.keys();
        // 遍历jsonObject数据，添加到Map对象
        Map<String,Object> map = new HashMap<String,Object>();
        while (it.hasNext())
        {
            String key = String.valueOf(it.next());
            if("words_result".equals(key)){
                JSONArray array = res.getJSONArray("words_result");
                array.forEach(name -> System.out.println(name));
            }
        }

    }
}