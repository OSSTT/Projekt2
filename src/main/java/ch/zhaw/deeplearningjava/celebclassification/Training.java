package ch.zhaw.deeplearningjava.celebclassification;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.TrainingResult;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.translate.TranslateException;
import com.google.gson.Gson;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public final class Training {

    // represents number of training samples processed before the model is updated
    private static final int BATCH_SIZE = 32;

    // the number of passes over the complete dataset
    private static final int EPOCHS = 32;

    public static void main(String[] args) throws IOException, TranslateException {
        // the location to save the model
        Path modelDir = Paths.get("models");

        // create ImageFolder dataset from directory
        ImageFolder dataset = initDataset("celebrityFacesDataset");
        // Split the dataset set into training dataset and validate dataset
        RandomAccessDataset[] datasets = dataset.randomSplit(8, 2);

        // set loss function, which seeks to minimize errors
        // loss function evaluates model's predictions against the correct answer
        // (during training)
        // higher numbers are bad - means model performed poorly; indicates more errors;
        // want to
        // minimize errors (loss)
        Loss loss = Loss.softmaxCrossEntropyLoss();

        // setting training parameters (ie hyperparameters)
        TrainingConfig config = setupTrainingConfig(loss);

        Model model = Models.getModel(); // empty model instance to hold patterns
        Trainer trainer = model.newTrainer(config);
        // metrics collect and report key performance indicators, like accuracy
        trainer.setMetrics(new Metrics());

        Shape inputShape = new Shape(1, 3, Models.IMAGE_HEIGHT, Models.IMAGE_HEIGHT);

        // initialize trainer with proper input shape
        trainer.initialize(inputShape);

        // find the patterns in data
        EasyTrain.fit(trainer, EPOCHS, datasets[0], datasets[1]);

        // set model properties
        TrainingResult result = trainer.getTrainingResult();
        model.setProperty("Epoch", String.valueOf(EPOCHS));
        model.setProperty(
                "Accuracy", String.format("%.5f", result.getValidateEvaluation("Accuracy")));
        model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));

        // save the model after done training for inference later
        // model saved as celebclassifier-0032.params
        model.save(modelDir, Models.MODEL_NAME);

        // save labels into model directory
        Models.saveSynset(modelDir, dataset.getSynset());

         // Save training results as JSON
         saveTrainingResults(modelDir.resolve("training_results.json"), BATCH_SIZE, EPOCHS, result.getValidateEvaluation("Accuracy"), result.getValidateLoss());

    }


    private static ImageFolder initDataset(String datasetRoot)
            throws IOException, TranslateException {
        ImageFolder dataset = ImageFolder.builder()
                // retrieve the data
                .setRepositoryPath(Paths.get(datasetRoot))
                .optMaxDepth(99)
                .addTransform(new Resize(Models.IMAGE_WIDTH, Models.IMAGE_HEIGHT))
                .addTransform(new ToTensor())
                // random sampling; don't process the data in order
                .setSampling(BATCH_SIZE, true)
                .build();

        dataset.prepare();
        return dataset;
    }

    private static TrainingConfig setupTrainingConfig(Loss loss) {
        return new DefaultTrainingConfig(loss)
                .addEvaluator(new Accuracy())
                .addTrainingListeners(TrainingListener.Defaults.logging());
    }


    private static void saveTrainingResults(Path filePath, int batchSize, int epochs, double accuracy, double loss) throws IOException {
        Map<String, Object> results = new HashMap<>();
        results.put("Batch_Size", batchSize);
        results.put("Epochs", epochs);
        results.put("Accuracy", accuracy);
        results.put("Loss", loss);

        Gson gson = new Gson();
        String json = gson.toJson(results);

        try (FileWriter writer = new FileWriter(filePath.toString())) {
            writer.write(json);
        }
    }


}
