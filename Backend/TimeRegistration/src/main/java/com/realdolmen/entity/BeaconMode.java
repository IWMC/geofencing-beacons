package com.realdolmen.entity;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * // TODO: 3/05/2016 uitleg
 */
@Embeddable
@XmlRootElement
public class BeaconMode {

    private boolean rangeMode;
    private double meters;

    public BeaconMode() {
    }

    public BeaconMode(boolean rangeMode) {
        this.rangeMode = rangeMode;
    }

    public BeaconMode(boolean rangeMode, double meters) {
        this.meters = meters;
        this.rangeMode = rangeMode;
    }

    public boolean isRangeMode() {
        return rangeMode;
    }

    public void setRangeMode(boolean rangeMode) {
        this.rangeMode = rangeMode;
    }

    public double getMeters() {
        return meters;
    }

    public void setMeters(double meters) {
        this.meters = meters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeaconMode)) return false;

        BeaconMode that = (BeaconMode) o;

        if (rangeMode != that.rangeMode) return false;
        return Double.compare(that.meters, meters) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (rangeMode ? 1 : 0);
        temp = Double.doubleToLongBits(meters);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
