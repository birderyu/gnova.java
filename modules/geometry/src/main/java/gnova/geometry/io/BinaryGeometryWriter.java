package gnova.geometry.io;

import gnova.core.annotation.NotNull;
import gnova.geometry.model.*;

import java.io.IOException;
import java.io.OutputStream;

public interface BinaryGeometryWriter
        extends GeometryWriter<byte[]>, BinaryGeometryIOSettings {

    /**
     * 将点对象转换成二进制对象
     *
     * @param point 点对象，不允许为null
     * @return 二进制对象，不会返回null
     * @throws GeometryIOException 若转换失败，则抛出此异常
     */
    @NotNull
    byte[] writePoint(@NotNull Point point) throws GeometryIOException;

    /**
     * 将线串对象转换成二进制对象
     *
     * @param lineString 线串对象，不允许为null
     * @return 二进制对象，不会返回null
     * @throws GeometryIOException 若转换失败，则抛出此异常
     */
    @NotNull
    byte[] writeLineString(@NotNull LineString lineString) throws GeometryIOException;

    /**
     * 将线环对象转换成二进制对象
     *
     * @param linearRing 线环对象，不允许为null
     * @return 二进制对象，不会返回null
     * @throws GeometryIOException 若转换失败，则抛出此异常
     */
    @NotNull
    byte[] writeLinearRing(@NotNull LinearRing linearRing) throws GeometryIOException;

    /**
     * 将多边形对象转换成二进制对象
     *
     * @param polygon 多边形对象，不允许为null
     * @return 二进制对象，不会返回null
     * @throws GeometryIOException 若转换失败，则抛出此异常
     */
    @NotNull
    byte[] writePolygon(@NotNull Polygon polygon) throws GeometryIOException;

    /**
     * 将线串对象转换成二进制对象
     *
     * @param geometries 几何集合对象，不允许为null
     * @return 二进制对象，不会返回null
     * @throws GeometryIOException 若转换失败，则抛出此异常
     */
    @NotNull
    byte[] writeGeometryCollection(@NotNull GeometryCollection geometries) throws GeometryIOException;

    /**
     * 将多多边形对象转换成二进制对象
     *
     * @param multiPoint 多多边形对象，不允许为null
     * @return 二进制对象，不会返回null
     * @throws GeometryIOException 若转换失败，则抛出此异常
     */
    @NotNull
    byte[] writeMultiPoint(@NotNull MultiPoint multiPoint) throws GeometryIOException;

    /**
     * 将多线串对象转换成二进制对象
     *
     * @param multiLineString 多线串对象，不允许为null
     * @return 二进制对象，不会返回null
     * @throws GeometryIOException 若转换失败，则抛出此异常
     */
    @NotNull
    byte[] writeMultiLineString(@NotNull MultiLineString multiLineString) throws GeometryIOException;

    /**
     * 将多多边形对象转换成二进制对象
     *
     * @param multiPolygon 多多边形对象，不允许为null
     * @return 二进制对象，不会返回null
     * @throws GeometryIOException 若转换失败，则抛出此异常
     */
    @NotNull
    byte[] writeMultiPolygon(@NotNull MultiPolygon multiPolygon) throws GeometryIOException;

    /**
     * 将一个几何对象写入到字节输出流
     *
     * @param geometry 几何对象，不允许为null
     * @param outputStream 字节输出流，不允许为null
     * @throws GeometryIOException 若写入失败，则抛出此异常
     */
    default void write(@NotNull Geometry geometry, @NotNull OutputStream outputStream)
            throws GeometryIOException {
        byte[] binaries = write(geometry);
        try {
            outputStream.write(binaries);
        } catch (IOException e) {
            throw new GeometryIOException(e);
        }
    }

    /**
     * 将一个点对象写入到字节输出流
     *
     * @param point 点对象，不允许为null
     * @param outputStream 字节输出流，不允许为null
     * @throws GeometryIOException 若写入失败，则抛出此异常
     */
    default void writePoint(@NotNull Point point, @NotNull OutputStream outputStream)
            throws GeometryIOException {
        byte[] binaries = writePoint(point);
        try {
            outputStream.write(binaries);
        } catch (IOException e) {
            throw new GeometryIOException(e);
        }
    }

    /**
     * 将一个线串对象写入到字节输出流
     *
     * @param lineString 线串对象，不允许为null
     * @param outputStream 字节输出流，不允许为null
     * @throws GeometryIOException 若写入失败，则抛出此异常
     */
    default void writeLineString(@NotNull LineString lineString, @NotNull OutputStream outputStream)
            throws GeometryIOException {
        byte[] binaries = writeLineString(lineString);
        try {
            outputStream.write(binaries);
        } catch (IOException e) {
            throw new GeometryIOException(e);
        }
    }

    /**
     * 将一个线环对象写入到字节输出流
     *
     * @param linearRing 线环对象，不允许为null
     * @param outputStream 字节输出流，不允许为null
     * @throws GeometryIOException 若写入失败，则抛出此异常
     */
    default void writeLinearRing(@NotNull LinearRing linearRing, @NotNull OutputStream outputStream)
            throws GeometryIOException {
        byte[] binaries = writeLinearRing(linearRing);
        try {
            outputStream.write(binaries);
        } catch (IOException e) {
            throw new GeometryIOException(e);
        }
    }

    /**
     * 将一个多边形对象写入到字节输出流
     *
     * @param polygon 多边形对象，不允许为null
     * @param outputStream 字节输出流，不允许为null
     * @throws GeometryIOException 若写入失败，则抛出此异常
     */
    default void writePolygon(@NotNull Polygon polygon, @NotNull OutputStream outputStream)
            throws GeometryIOException {
        byte[] binaries = writePolygon(polygon);
        try {
            outputStream.write(binaries);
        } catch (IOException e) {
            throw new GeometryIOException(e);
        }
    }

    /**
     * 将一个几何集合对象写入到字节输出流
     *
     * @param geometries 几何集合对象，不允许为null
     * @param outputStream 字节输出流，不允许为null
     * @throws GeometryIOException 若写入失败，则抛出此异常
     */
    default void writeGeometryCollection(@NotNull GeometryCollection geometries, @NotNull OutputStream outputStream)
            throws GeometryIOException {
        byte[] binaries = writeGeometryCollection(geometries);
        try {
            outputStream.write(binaries);
        } catch (IOException e) {
            throw new GeometryIOException(e);
        }
    }

    /**
     * 将一个多点对象写入到字节输出流
     *
     * @param multiPoint 多点对象，不允许为null
     * @param outputStream 字节输出流，不允许为null
     * @throws GeometryIOException 若写入失败，则抛出此异常
     */
    default void writeMultiPoint(@NotNull MultiPoint multiPoint, @NotNull OutputStream outputStream)
            throws GeometryIOException {
        byte[] binaries = writeMultiPoint(multiPoint);
        try {
            outputStream.write(binaries);
        } catch (IOException e) {
            throw new GeometryIOException(e);
        }
    }

    /**
     * 将一个多线串对象写入到字节输出流
     *
     * @param multiLineString 多线串对象，不允许为null
     * @param outputStream 字节输出流，不允许为null
     * @throws GeometryIOException 若写入失败，则抛出此异常
     */
    default void writeMultiLineString(@NotNull MultiLineString multiLineString, @NotNull OutputStream outputStream)
            throws GeometryIOException {
        byte[] binaries = writeMultiLineString(multiLineString);
        try {
            outputStream.write(binaries);
        } catch (IOException e) {
            throw new GeometryIOException(e);
        }
    }

    /**
     * 将一个多多边形对象写入到字节输出流
     *
     * @param multiPolygon 多多边形对象，不允许为null
     * @param outputStream 字节输出流，不允许为null
     * @throws GeometryIOException 若写入失败，则抛出此异常
     */
    default void writeMultiPolygon(@NotNull MultiPolygon multiPolygon, @NotNull OutputStream outputStream)
            throws GeometryIOException {
        byte[] binaries = writeMultiPolygon(multiPolygon);
        try {
            outputStream.write(binaries);
        } catch (IOException e) {
            throw new GeometryIOException(e);
        }
    }

}
