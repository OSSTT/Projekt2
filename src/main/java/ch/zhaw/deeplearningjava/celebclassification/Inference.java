package ch.zhaw.deeplearningjava.celebclassification;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class Inference {

    Predictor<Image, Classifications> predictor;

    public Inference() {
        try {
            // Connect to Azure Blob Storage
            String resourceGroupName = "thathjava";
            String containerName = "models";
            String accessKey = Config.getAzureStorageAccessKey();

            BlobServiceClientBuilder serviceClientBuilder = new BlobServiceClientBuilder()
                .connectionString("DefaultEndpointsProtocol=https;AccountName=" + resourceGroupName + ";AccountKey=" + accessKey + ";EndpointSuffix=core.windows.net");
            BlobContainerClient blobContainerClient = serviceClientBuilder.buildClient().getBlobContainerClient(containerName);

            // Download the model files from Azure Blob Storage
            downloadModelFile(blobContainerClient, "celebclassifier-0001.params", "azureModels");
            downloadModelFile(blobContainerClient, "synset.txt", "azureModels");

            // Load the model from the downloaded files
            Model model = Models.getModel();
            Path modelDir = Paths.get("azureModels");
            model.load(modelDir);

            // Define a translator for pre and post processing
            Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                    .addTransform(new Resize(Models.IMAGE_WIDTH, Models.IMAGE_HEIGHT))
                    .addTransform(new ToTensor())
                    .optApplySoftmax(true)
                    .build();
            predictor = model.newPredictor(translator);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadModelFile(BlobContainerClient containerClient, String fileName, String targetDirectory) {
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        blobClient.downloadToFile(targetDirectory + "/" + fileName);
    }

    public Classifications predict(byte[] image) throws ModelException, TranslateException, IOException {
        InputStream is = new ByteArrayInputStream(image);
        BufferedImage bi = ImageIO.read(is);
        Image img = ImageFactory.getInstance().fromImage(bi);

        Classifications predictResult = this.predictor.predict(img);
        return predictResult;
    }
}
