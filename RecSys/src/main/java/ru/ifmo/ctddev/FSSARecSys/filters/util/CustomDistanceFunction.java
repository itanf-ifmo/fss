package ru.ifmo.ctddev.FSSARecSys.filters.util;

import weka.core.*;
import weka.core.neighboursearch.PerformanceStats;

import java.util.Enumeration;
import java.util.Map;

public class CustomDistanceFunction extends EuclideanDistance implements DistanceFunction {
    private final Map<String, Map<String, Double>> map;
    private final String[] fssas;

    public CustomDistanceFunction(Map<String, Map<String, Double>> map, String[] FSSAs) {
        this.map = map;
        fssas = FSSAs;
    }

    /**
     * Sets the instances.
     *
     * @param insts the instances to use
     */
    @Override
    public void setInstances(Instances insts) {
//        System.out.println("setInstances(Instances insts)");
    }

    /**
     * returns the instances currently set.
     *
     * @return the current instances
     */
    @Override
    public Instances getInstances() {
        System.out.println("getInstances()");
        return null;
    }

    /**
     * Sets the range of attributes to use in the calculation of the distance. The
     * indices start from 1, 'first' and 'last' are valid as well. E.g.:
     * first-3,5,6-last
     *
     * @param value the new attribute index range
     */
    @Override
    public void setAttributeIndices(String value) {
        System.out.println("setAttributeIndices(String value)");

    }

    /**
     * Gets the range of attributes used in the calculation of the distance.
     *
     * @return the attribute index range
     */
    @Override
    public String getAttributeIndices() {
        System.out.println("getAttributeIndices()");
        return null;
    }

    /**
     * Sets whether the matching sense of attribute indices is inverted or not.
     *
     * @param value if true the matching sense is inverted
     */
    @Override
    public void setInvertSelection(boolean value) {
        System.out.println("setInvertSelection(boolean value)");

    }

    /**
     * Gets whether the matching sense of attribute indices is inverted or not.
     *
     * @return true if the matching sense is inverted
     */
    @Override
    public boolean getInvertSelection() {
        System.out.println("getInvertSelection()");
        return false;
    }

    /**
     * Calculates the distance between two instances.
     *
     * @param first  the first instance
     * @param second the second instance
     * @return the distance between the two given instances
     */
    @Override
    public double distance(Instance first, Instance second) {
        return map.get(fssas[(int)first.value(1)]).get(fssas[(int)second.value(1)]);
    }

    /**
     * Calculates the distance between two instances.
     *
     * @param first  the first instance
     * @param second the second instance
     * @param stats  the performance stats object
     * @return the distance between the two given instances
     */
    @Override
    public double distance(Instance first, Instance second, PerformanceStats stats) {
        System.out.println("distance(Instance first, Instance second, PerformanceStats stats)");
        return 0;
    }

    /**
     * Calculates the distance between two instances. Offers speed up (if the
     * distance function class in use supports it) in nearest neighbour search by
     * taking into account the cutOff or maximum distance. Depending on the
     * distance function class, post processing of the distances by
     * postProcessDistances(double []) may be required if this function is used.
     *
     * @param first       the first instance
     * @param second      the second instance
     * @param cutOffValue If the distance being calculated becomes larger than
     *                    cutOffValue then the rest of the calculation is discarded.
     * @return the distance between the two given instances or
     * Double.POSITIVE_INFINITY if the distance being calculated becomes
     * larger than cutOffValue.
     */
    @Override
    public double distance(Instance first, Instance second, double cutOffValue) {
        double d = distance(first, second);
        return d < cutOffValue ? d : Double.POSITIVE_INFINITY;
    }

    /**
     * Calculates the distance between two instances. Offers speed up (if the
     * distance function class in use supports it) in nearest neighbour search by
     * taking into account the cutOff or maximum distance. Depending on the
     * distance function class, post processing of the distances by
     * postProcessDistances(double []) may be required if this function is used.
     *
     * @param first       the first instance
     * @param second      the second instance
     * @param cutOffValue If the distance being calculated becomes larger than
     *                    cutOffValue then the rest of the calculation is discarded.
     * @param stats       the performance stats object
     * @return the distance between the two given instances or
     * Double.POSITIVE_INFINITY if the distance being calculated becomes
     * larger than cutOffValue.
     */
    @Override
    public double distance(Instance first, Instance second, double cutOffValue, PerformanceStats stats) {
        System.out.println("distance(Instance first, Instance second, double cutOffValue, PerformanceStats stats)");
        return 0;
    }

    /**
     * Does post processing of the distances (if necessary) returned by
     * distance(distance(Instance first, Instance second, double cutOffValue). It
     * may be necessary, depending on the distance function, to do post processing
     * to set the distances on the correct scale. Some distance function classes
     * may not return correct distances using the cutOffValue distance function to
     * minimize the inaccuracies resulting from floating point comparison and
     * manipulation.
     *
     * @param distances the distances to post-process
     */
    @Override
    public void postProcessDistances(double[] distances) {
        System.out.println("postProcessDistances(double[] distances)");
    }

    /**
     * Update the distance function (if necessary) for the newly added instance.
     *
     * @param ins the instance to add
     */
    @Override
    public void update(Instance ins) {
        System.out.println("update(Instance ins)");
    }

    /**
     * Free any references to training instances
     */
    @Override
    public void clean() {
//        System.out.println("clean()");
    }

    /**
     * Returns an enumeration of all the available options..
     *
     * @return an enumeration of all available options.
     */
    @Override
    public Enumeration<Option> listOptions() {
        System.out.println("Enumeration<Option> listOptions()");
        return null;
    }

    /**
     * Sets the OptionHandler's options using the given list. All options
     * will be set (or reset) during this call (i.e. incremental setting
     * of options is not possible).
     *
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    @Override
    public void setOptions(String[] options) throws Exception {
        System.out.println("setOptions(String[] options)");
    }

    /**
     * Gets the current option settings for the OptionHandler.
     *
     * @return the list of current option settings as an array of strings
     */
    @Override
    public String[] getOptions() {
        System.out.println("getOptions()");
        return new String[0];
    }
}
