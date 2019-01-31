package gnova.geometry.model;

import gnova.core.annotation.NotNull;
import gnova.geometry.model.pattern.Puntal;
import gnova.core.ReadOnlyIterator;

/**
 * 多点
 * 
 * @author Birderyu
 * @date 2017/6/21
 */
public interface MultiPoint
        extends GeometryCollection<Point>, Puntal {

    default Point getPointAt(int n) {
        return getGeometryAt(n);
    }

    @Override
    @NotNull
    default ReadOnlyIterator<Point> iterator() {
        return new MultiPointIterator(this, 0);
    }

    @Override
    @NotNull
    default GeometryType getType() {
        return GeometryType.MultiPoint;
    }

    @Override
    default int getDimension() {
        return Puntal.DIMENSION;
    }

    @Override
    @NotNull
    default Geometry getBoundary() {
        return NONE;
    }

    @Override
    MultiPoint reverse();

    @Override
    MultiPoint normalize();

    @Override
    MultiPoint clone();

}
