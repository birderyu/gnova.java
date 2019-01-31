package gnova.geometry.model.impl.jts;

import gnova.geometry.model.LinearRing;

/**
 * Created by Birderyu on 2017/6/23.
 */
final class LinearRingAdaptor
        extends LineStringAdaptor implements LinearRing {

    public LinearRingAdaptor(org.locationtech.jts.geom.LinearRing jtsLinearRing) {
        super(jtsLinearRing);
    }

    @Override
    public org.locationtech.jts.geom.LinearRing getJts() {
        return (org.locationtech.jts.geom.LinearRing) super.getJts();
    }

    @Override
    public LinearRing reverse() {
        return (LinearRing) super.reverse();
    }

    @Override
    public LinearRing normalize() {
        return (LinearRing) super.clone();
    }

    @Override
    public LinearRing clone() {
        return (LinearRing) super.clone();
    }
}
