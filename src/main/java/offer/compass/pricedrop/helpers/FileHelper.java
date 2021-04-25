package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Component
@Slf4j
public class FileHelper {

    void createImageFromBufferedImage(BufferedImage image, String pathToSave, String folderPath) throws IOException {
        this.createNewDirectory(folderPath);
        pathToSave = this.renameFile(pathToSave);
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        ImageIO.write(copy, Constant.IMAGE_FORMAT_V2, new File(pathToSave));
    }

    private void createNewDirectory(String folderPath) {
        File file = new File(folderPath);
        if (!file.exists()) {
            boolean isCreated = file.mkdirs();
            if (isCreated)
                log.info("New folder created, path is " + folderPath);
        }
    }

    private String renameFile(String fileName) {
        if (new File(fileName).isFile()) {
            log.info("File with same name found. Renaming it...");
            fileName = fileName.replace(Constant.IMAGE_FORMAT, "") + "-copy" + Constant.IMAGE_FORMAT;
            return renameFile(fileName);
        }
        return fileName;
    }

    void takeScreenshot(WebDriver browser, String dept, int count, boolean isAmazon) throws IOException {
        File srcFile = ((TakesScreenshot) browser).getScreenshotAs(OutputType.FILE);
        BufferedImage image = ImageIO.read(srcFile);
        String folderPath = Constant.PATH_TO_SAVE_THUMBNAIL + dept + "-" + LocalDate.now() + Constant.UTIL_PATH_SLASH;
        String pathToSave;
        if (isAmazon)
            pathToSave = folderPath + (count+1)+ "-SS-Amazon" + Constant.IMAGE_FORMAT;
        else
            pathToSave = folderPath + (count+1)+ "-SS-Flipkart" + Constant.IMAGE_FORMAT;
        this.createImageFromBufferedImage(image, pathToSave, folderPath);
    }
}
