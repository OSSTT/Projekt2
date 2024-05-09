package ch.zhaw.deeplearningjava.celebclassification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
public class ClassificationController {


    private Inference inference = new Inference();

    @GetMapping("/ping")
    public String ping() {
        return "Classification app is up and running!";
    }

    @PostMapping(path = "/analyze")
    public String predict(@RequestParam("image") MultipartFile image) throws Exception {
        try {
            byte[] imageData = image.getBytes();
            return inference.predict(imageData).toJson();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Failed to process image.", e);
        }
    }
}
