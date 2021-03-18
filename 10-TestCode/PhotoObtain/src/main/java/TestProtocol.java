
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.awt.*;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


@RunWith(SpringRunner.class)
public class TestProtocol {


    @Test
    public void test2() {
//        long start = System.currentTimeMillis();                  //水印字体
//        String srcImgPath="D:\\image\\sy\\IMG_20190910_165423.jpg"; //源图片地址
//        //String srcImgPath="D:\\image\\sy\\IMG_20190910_174905.jpg"; //源图片地址 有时间无经纬度
//        //String srcImgPath="D:\\image\\sy\\IMG_20190910_175051.jpg"; //源图片地址
//        String tarImgPath="D:\\2.jpg"; //待存储的地址
//        Color color = Color.red;
//        WatermarkUtil watermarkUtil = new WatermarkUtil();
//        Watermark watermark = watermarkUtil.getLocationAndTime(srcImgPath);
//        watermark.setSiteName("相公李家东");
//        watermark.setAmmeterNumber("3730001000000427172320");
//
//        watermarkUtil.addWaterMark(srcImgPath, tarImgPath, watermark,color, 0.03);
//        long end = System.currentTimeMillis();
//        System.out.println("消耗时间："+(end-start));
    }

    @Test
    public void test3(){
        WatermarkUtil watermarkUtil = new WatermarkUtil();
        String srcImgPath="H:\\FloorMeterTakePhoto\\ChaoBiao\\202102\\000qvaHhSt.Jpg";
        Watermark watermark = watermarkUtil.getLocationAndTime(srcImgPath);
//        if(StringUtils.isEmpty(watermark.getLatitude()) || StringUtils.isEmpty(watermark.getLongitude())){
//            System.out.println("****test3 456*****");
//        }
//        System.out.println("纬度:"+watermark.getLatitude());
//        System.out.println("经度:"+watermark.getLongitude());

    }

    @Test
    public void phono1(){
        System.out.print("please input image path:");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();
        File file = new File("path");
        if(!file.exists()) {
            System.err.println("file not exist!");
            scanner.close();
            return;
        }
        System.out.print("please setLanguage(eng/chi_sim):");
        String language = scanner.nextLine();
        scanner.close();
        if(!("chi_sim".equals(language) || "eng".equals(language))) {
            System.err.println("language must be chi_sim or eng!");
            return;
        }
        ITesseract instance = new Tesseract();
        instance.setDatapath(System.getProperty("user.dir") + "\\tessdata"); // 语言库位置
        instance.setLanguage(language);// chi_sim:简体中文,eng:英文
        String result = null;
        try {
            result = instance.doOCR(file);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        System.out.println("result: ");
        System.out.println(result);
    }


}
