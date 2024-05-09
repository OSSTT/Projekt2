package ch.zhaw.deeplearningjava.celebclassification;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import java.io.File;

public class ModelUploader {

    public static void main(String[] args) {
        String resourceGroupName = "thathjava";
        String containerName = "models";
        String accessKey = Config.getAzureStorageAccessKey();
        String modelsFolder = "models";

        uploadFile(resourceGroupName, containerName, accessKey, modelsFolder, "celebclassifier-0001.params");
        uploadFile(resourceGroupName, containerName, accessKey, modelsFolder, "synset.txt");
    }

    private static void uploadFile(String resourceGroupName, String containerName, String accessKey,
                                   String folderName, String fileName) {
        String filePath = folderName + File.separator + fileName;
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder();
        builder.endpoint("https://" + resourceGroupName + ".blob.core.windows.net")
                .credential(new StorageSharedKeyCredential(resourceGroupName, accessKey));

        BlobContainerClient containerClient = builder.buildClient().getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        blobClient.uploadFromFile(filePath);
        System.out.printf("File %s uploaded successfully.%n", fileName);
    }
}
