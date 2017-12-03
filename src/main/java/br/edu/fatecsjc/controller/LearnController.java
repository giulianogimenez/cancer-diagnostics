package br.edu.fatecsjc.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

import org.datavec.api.split.InputSplit;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.CropImageTransform;
import org.datavec.image.transform.FlipImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.ScaleImageTransform;
import org.datavec.image.transform.WarpImageTransform;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.PretrainedType;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import com.google.common.io.Files;

import br.edu.fatecsjc.model.TestsetModel;
import br.edu.fatecsjc.model.TrainsetModel;

public class LearnController {
	
	public void learn() throws IOException {
		TrainsetModel trainsetModel = new TrainsetModel();
		ImageTransform[] transforms = getTransforms();
		ComputationGraph initializedZooModel  = (ComputationGraph) trainsetModel.getZooModel().initPretrained(PretrainedType.IMAGENET);
		FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
	            .learningRate(0.0001)
	            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	            .updater(Updater.NESTEROVS)
	            .seed(123)
	            .build();
		System.out.println(initializedZooModel.summary());
		ComputationGraph modelTransfer = new TransferLearning.GraphBuilder(initializedZooModel)
			    .fineTuneConfiguration(fineTuneConf)
			              .setFeatureExtractor("flatten_3")
			              .removeVertexKeepConnections("fc1000")
			              .addLayer("fc1000", 			        
			        new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
			                        .nIn(2048).nOut(trainsetModel.N_LABELS)
			                        .weightInit(WeightInit.XAVIER)
			                        .activation(Activation.SOFTMAX).build(), "flatten_3")
			              .build();
		System.out.println(modelTransfer.summary());
		modelTransfer.setListeners(new ScoreIterationListener(trainsetModel.MINI_BATCH_SIZE));
		for (int i = 0; i < trainsetModel.numEpochs; i++) {
			modelTransfer.fit(trainsetModel.returnModelDataSetIterator());
		}

		for (int i = 0; i < trainsetModel.transformedDataEpochs; i++) {
			for (int j = 0; j < transforms.length; j++) {
				ImageTransform imageTransform = transforms[j];
				ImageRecordReader trainReader = trainsetModel.getTrainReader();
				trainReader.initialize((InputSplit) trainsetModel.returnModelDataSetIterator(), imageTransform);
				trainsetModel.setTrainDataSet(new RecordReaderDataSetIterator(trainReader, trainsetModel.MINI_BATCH_SIZE, 1, trainsetModel.N_LABELS));
				modelTransfer.fit(trainsetModel.returnModelDataSetIterator());
			}
		}

		Evaluation eval = new Evaluation(trainsetModel.N_LABELS);
		TestsetModel testsetModel = new TestsetModel();
		while (testsetModel.returnModelDataSetIterator().hasNext()) {
			DataSet next = testsetModel.returnModelDataSetIterator().next();
			INDArray[] output = modelTransfer.output(next.getFeatureMatrix());
			for (int i = 0; i < output.length; i++) {
				eval.eval(next.getLabels(), output[i]);
			}
		}
	}


	private static ImageTransform[] getTransforms() {
		ImageTransform randCrop = new CropImageTransform(new Random(), 10);
		ImageTransform warpTransform = new WarpImageTransform(new Random(), 42);
		ImageTransform flip = new FlipImageTransform(new Random());
		ImageTransform scale = new ScaleImageTransform(new Random(), 1);
		return new ImageTransform[] { randCrop, warpTransform, flip, scale };
	}
}
