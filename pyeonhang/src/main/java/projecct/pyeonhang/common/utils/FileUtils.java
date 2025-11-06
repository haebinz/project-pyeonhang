    package projecct.pyeonhang.common.utils;

    import com.mortennobel.imagescaling.AdvancedResizeOp;
    import com.mortennobel.imagescaling.MultiStepRescaleOp;
    import org.springframework.stereotype.Component;
    import org.springframework.web.multipart.MultipartFile;

    import javax.imageio.ImageIO;
    import java.awt.image.BufferedImage;
    import java.io.BufferedInputStream;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.InputStream;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.UUID;

    @Component
    public class FileUtils {

        /**
         * 파일 업로드 기능
         */
        public Map<String, Object> uploadFiles(MultipartFile file, String filePath) throws Exception{
            Map<String, Object>   resultMap = new HashMap<>();

            if(file == null || file.isEmpty()) {
                return null;
            }

            String fileName = file.getOriginalFilename();
            String extention = fileName.substring(fileName.lastIndexOf(".")+1) ;
            String randName = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
            String storedFileName = randName + "." + extention;

            String fullPath = filePath + storedFileName;

            File newFile = new File(fullPath);

            //경로없으면 만들어주자
            if( !newFile.getParentFile().exists()) {
                //경로 만들어주기
                newFile.getParentFile().mkdirs();
            }

            newFile.createNewFile(); //빈파일
            file.transferTo(newFile);

            resultMap.put("fileName", fileName);
            resultMap.put("storedFileName", storedFileName);
            resultMap.put("filePath", filePath);

            return resultMap;
        }




        /**
         * 파일 삭제
         */
        public void deleteFile(String filePath) throws Exception{
            File deleteFile = new File(filePath);

            if(deleteFile.exists()) {
                deleteFile.delete();
            }
        }


        /**
         * 썸네일 만들기
         */
        public String thumbNailFile(int width, int height, File originFile, String thumbPath) throws Exception {
            String thumbFileName = "";

            String fileName = originFile.getName();
            String extention = fileName.substring(fileName.lastIndexOf(".")+1) ;
            String randName = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
            thumbFileName = randName + "." + extention;


            try(
                    InputStream in = new FileInputStream(originFile);
                    BufferedInputStream bf = new BufferedInputStream(in);
            ){

                //원본 이미지 파일 뜨기
                BufferedImage originImage = ImageIO.read(originFile);
                //이미지 사이즈 줄이기
                MultiStepRescaleOp scaleImage = new MultiStepRescaleOp(width, height);
                //마스킹처리
                scaleImage.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);
                //리사이즈 이미지 생성
                BufferedImage resizeImage = scaleImage.filter(originImage, null);

                String thubmbFilePath = thumbPath  + thumbFileName;

                File resizeFile = new File(thubmbFilePath);

                //경로없으면 만들어주자
                if( !resizeFile.getParentFile().exists()) {
                    //경로 만들어주기
                    resizeFile.getParentFile().mkdirs();
                }

                //리사이즈한 파일을 실제 경로에 생성. 결과를 리턴해준다.
                boolean isWrite = ImageIO.write(resizeImage, extention,  resizeFile);

                if (!isWrite) {
                    throw  new  RuntimeException("썸네일 생성 오류");
                }


            }catch(Exception e){
                thumbFileName = null;
                e.printStackTrace();
                throw new RuntimeException("썸네일 생성 오류 ");
            }

            return thumbFileName;

        }
    }
