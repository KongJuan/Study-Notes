import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.Scanner;

public class WaterMarkTest {
    public static void main(String[] args) {
        File file = new File("H:\\FloorMeterTakePhoto\\ChaoBiao\\202102\\00e9AwP049.Jpg");
        if(!file.exists()) {
            System.err.println("file not exist!");
            //scanner.close();
            return;
        }
        Tesseract instance = new Tesseract();
        instance.setDatapath(System.getProperty("user.dir") + "\\tessdata"); // 语言库位置
        instance.setLanguage("chi_sim");// chi_sim:简体中文,eng:英文
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
