/**
 * Implements a Weka filter which computes the relevance of each instance in a data set, based n a clustering step
 */
package weka.filters.unsupervised.instance;

import weka.clusterers.XMeans;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;
import weka.filters.unsupervised.attribute.Remove;
import weka.core.Instances;

/**
 * @author Lucian Sasu
 * lmsasu <at> yahoo dot com
 */
public class RelevanceClusteringClosestCentrLowRel extends SimpleBatchFilter {

	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 2L;
	
	//to avoid division by zero
	final double epsilon = 1e-3;

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
	 */
	@Override
	protected Instances determineOutputFormat(Instances arg0) throws Exception {
		return arg0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.filters.SimpleFilter#globalInfo()
	 */
	@Override
	public String globalInfo() {
		return "Computes relevance scores for data instances, based on clustering";
	}
	
	@Override
	/**
	 * Gets the capabilities; useful for Weka environment
	 */
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.enableAllAttributes();
		result.enableAllClasses();
		result.enable(Capability.NO_CLASS);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
	@Override
	protected Instances process(Instances instances) throws Exception {
		
		if (instances.numInstances() == 1) {
			return instances;//nothing to cluster
		} else {
			// cluster through xmeans and get the centroids
			
			Instances trainWOClasses = removeClass(instances);
			//cluster
			Instances centers = getCentroids(trainWOClasses);

			for (int i = 0; i < instances.numInstances(); i++) {
				double[] distances = computeDistances(trainWOClasses.get(i), centers);
				double minDistance = getMin(distances);
				double relevance = minDistance;//small distance => small relevance
				instances.get(i).setWeight(relevance);
			}
		}
		
		changeRelevancesToMin1(instances);

		return instances;
	}

	/**
	 * The current relevances are changed to be at least 1
	 * @param instances the set of instance whose weights (relevances) are to be updated
	 */
	private void changeRelevancesToMin1(Instances instances) {
		double minWeight = Double.POSITIVE_INFINITY;
		for(Instance instance : instances){
			minWeight = Math.min(minWeight, instance.weight());
		}
		for(Instance instance : instances){
			instance.setWeight(instance.weight() / (epsilon + minWeight));
		}
	}

	/***
	 * Return the minimum value form a vector
	 * 
	 * @param values
	 *            a vector of double values
	 * @return the minimum value within the vector
	 */
	private static double getMin(final double[] values) {
		double min = values[0];
		for (int i = 1; i < values.length; i++) {
			min = Math.min(min, values[i]);
		}
		return min;
	}

	/***
	 * Computes the distance between an instance an the cluster centroids
	 * 
	 * @param instance
	 *            the data row (class missing) for which the distance to
	 *            centroids must be computed
	 * @param centroids
	 *            the centroids as computed in the clustering step
	 * @return a vector of double value containing the distances from instance
	 *         to all centroids
	 */
	private static double[] computeDistances(Instance instance, Instances centroids) {
		double[] distances = new double[centroids.numInstances()];

		for (int i = 0; i < centroids.numInstances(); i++) {
			Instance centroid = centroids.get(i);
			distances[i] = computeDistance(instance, centroid);
		}

		return distances;
	}

	/***
	 * Computes the distance between an instance and a centroid
	 * 
	 * @param instance
	 *            a data row (class ommited) for which the distance to the given
	 *            centroid must be computed
	 * @param centroid
	 *            a cluster centroid
	 * @return a double value
	 */
	private static double computeDistance(Instance instance, Instance centroid) {
		double sum = 0.0;
		for (int i = 0; i < instance.numAttributes(); i++) {
			sum += Math.pow(instance.value(i) - centroid.value(i), 2);
		}
		return Math.sqrt(sum);
	}

	/***
	 * Applies a clustering algorithm for a data set
	 * 
	 * @param instances
	 *            the instances to be clustered
	 * @return a set of centroids
	 * @throws Exception
	 */
	private static Instances getCentroids(Instances instances) throws Exception {
		XMeans clusterer = new XMeans();
		clusterer.setMaxNumClusters(1000);//TODO: expose as hyperparameter
		clusterer.setMinNumClusters(2);//TODO: expose as hyperparameter
		clusterer.setMaxIterations(1000);//TODO: expose as hyperparameter
		clusterer.buildClusterer(instances);

		Instances centroids = clusterer.getClusterCenters();
		return centroids;
	}

	/**
	 * Removes the class from an instance
	 * 
	 * @param instances
	 *            a set of weka instances with classes
	 * @return a set of weka instances without classes
	 * @throws Exception
	 *             by weka internals
	 */
	private static Instances removeClass(Instances instances) throws Exception {
		Instances instancesWOClasses = new Instances(instances);
		Remove remove = new Remove();
		System.out.println("class index to be removed: " + (1 + instancesWOClasses.classIndex()));
		System.out.println("Instances count: " + instances.numInstances());
		remove.setAttributeIndices(1 + instancesWOClasses.classIndex() + "");
		remove.setInputFormat(instancesWOClasses);
		instancesWOClasses = Filter.useFilter(instancesWOClasses, remove);
		return instancesWOClasses;
	}
}
