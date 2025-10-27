package utils;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.MultiStepRescaleOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * 이미지 scaling by shin
 */
public class ImageUtils {
    private static final  Logger logger =  LoggerFactory.getLogger(ImageUtils.class);

    /**
     * 이미지 url
     * 저장할 경로
     * 저장할 이름 
     * @param siteUrl  
     * @param copyFilePath
     * @param fileName
     * @return
     */
    public static File makeFileFromURL(String siteUrl, String copyFilePath, String fileName) {

        File newFile = null;

        try {
            //파일 쓰기
            URL website = new URL(siteUrl);
            File file = new  File(copyFilePath + fileName);

            ReadableByteChannel rbc = Channels.newChannel(website.openStream());

            //존재하지 않는다면.
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
           
                //폴더는 있는데 파일이 없다.
            }else if(!file.exists()) {
	            //아웃풋 만들고
	            FileOutputStream fos = new FileOutputStream(file);
	            //nio를 쓰면 부하나 속도면에서 더 좋다
	            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	            newFile = file;
	            logger.error("success write file {} " + file.getName());
            } else {
            	//폴더도 있고 파일도 있다.
            	logger.debug("success is already Exist {} " + file.getName());
            	newFile = file;
            }

        }catch (Exception e) {
            logger.error("error write file , {}", e.getMessage());
        }
        return newFile;
    }

    /**
     * 이미지 리사이즈
     * @param file
     * @param filePath
     * @param width
     * @param height
     * @return
     */
    public static File  resizeImage(File file, String filePath, int width, int height) {

        String defaultFolder = filePath + "/thumb/";

        File thumbFile = null;
        boolean resultCode = false;

        try {

            if(filePath != null && filePath.length() != 0) {
                String orignFileName = file.getName();  //파일이름 가져오기
                String ext = orignFileName.substring(orignFileName.lastIndexOf(".")+1); //확장자 찾기
                String thumbFileName = orignFileName.substring(0, orignFileName.lastIndexOf(".")) +"_thumb."+ext;
                //버퍼이미지 생성
                BufferedImage img = ImageIO.read(new FileInputStream(file));
                //리사이즈
                MultiStepRescaleOp rescale = new MultiStepRescaleOp(width, height);
                //마스크 입히기...
                rescale.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);
                //이미지 생성
                BufferedImage resizeImg = rescale.filter(img, null);
                //thubNailPath
                String fullPath = defaultFolder + thumbFileName;

                //스케일된 파일객체 생성
                File out = new File(fullPath);

                //존재하지 않으면 만든다
                if(!out.getParentFile().exists()) {
                    out.getParentFile().mkdirs();
                }else if(!out.exists()) {

                    //파일쓰기
                    resultCode = ImageIO.write(resizeImg, ext, out);

                    //성공 여부
                    if (resultCode) {
                        thumbFile = out;
                        logger.debug("success create ThumbnailFile  name {}" + thumbFileName);
                    } else {
                        logger.debug("fail create ThumbnailFile  name {}" + thumbFileName);
                    }
                } else {
                    //만들지 않고 그냥 객체만 보낸다.
                	logger.debug("success is already Exist {} " + file.getName());
                    thumbFile = out;
                }
            }else {
                logger.debug("no File Path");
            }

        }catch (Exception e) {
            logger.error("fail create ThumbnailFile,  {}" + e.getMessage());
        }

        return thumbFile;
    }

}
