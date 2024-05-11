package ch.zhaw.deeplearningjava.celebclassification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.util.StreamUtils;
import java.io.FileInputStream;
import java.io.File;


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

    @GetMapping("/training_result")
    public ResponseEntity<String> getTrainingResultJson() throws IOException {
        // Pfad zur Datei training_result.json
        String filePath = "/usr/src/app/stats/training_results.json";
        File file = new File(filePath);

        try {
            FileInputStream inputStream = new FileInputStream(file);
            // Lese den Inhalt der Datei
            String fileContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            // Baue die Antwort und gebe den Dateiinhalt mit dem entsprechenden Mediatyp zurück
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fileContent);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File training_results.json not found");
        }
    }
    
}
