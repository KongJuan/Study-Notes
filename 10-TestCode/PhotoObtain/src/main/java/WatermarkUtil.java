import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 * 图片添加文字水印，读取手机相机的图片位置及时间信息
 */
public class WatermarkUtil {
    /**
     * 图片添加文字水印的方法
     *      srcImgPath 源图片路径
     *      tarImgPath 保存的图片路径
     *      watermark  水印内容
     *      color      水印颜色
     *      font       水印字体
     */
    public void addWaterMark(String srcImgPath, String tarImgPath,
                             Watermark watermark,
                             Color color, double percentage) {
        FileOutputStream outImgStream =null;
        try {
            // 读取原图片信息
            File srcImgFile = new File(srcImgPath);//得到文件
            Image srcImg = ImageIO.read(srcImgFile);//文件转化为图片
            int srcImgWidth = srcImg.getWidth(null);//获取图片的宽
            int srcImgHeight = srcImg.getHeight(null);//获取图片的高
            // 加水印
            BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bufImg.createGraphics();
            g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
            g.setColor(color); //根据图片的背景设置水印颜色
            //设置字体
            Double d = Math.ceil(srcImgHeight*percentage);
            int fontSize = d.intValue();
            Font font = new Font("宋体", Font.PLAIN, fontSize);
            g.setFont(font);
            //设置水印的坐标
            //int watermarkLength = getWatermarkLength(waterMarkContent, g);//获取水印内容长度
//            if(!StringUtils.isEmpty(watermark.getSiteName())){
//                g.drawString("站址名称："+watermark.getSiteName(), 0, fontSize+15);                           //画出第一道水印
//                //
//            }
            g.dispose();
            // 输出图片
            outImgStream = new FileOutputStream(tarImgPath);
            ImageIO.write(bufImg, "jpg", outImgStream);
            System.out.println("添加水印完成");
            outImgStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(outImgStream!=null){
                try {
                    outImgStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public int getWatermarkLength(String waterMarkContent, Graphics2D g) {
        return g.getFontMetrics(g.getFont()).charsWidth(waterMarkContent.toCharArray(), 0, waterMarkContent.length());
    }


    /**
     * 获取图片中的经纬度及拍摄时间
     */
    public Watermark getLocationAndTime(String imagePath){
        try {
            File jpegFile = new File(imagePath);
            Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);
            String registNo = "";
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    System.out.println("名称:" + tag.getTagName() + "******值:" + tag.getDescription());
                }
            }
            return new Watermark(registNo);
        }catch(Exception e){
            e.printStackTrace();
            return new Watermark();
        }
    }





}