/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.csw.services.getrecords.es;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

import org.fao.geonet.kernel.csw.services.getrecords.IFieldMapper;
import org.geotools.filter.expression.AbstractExpressionVisitor;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class Expression2CswVisitor extends AbstractExpressionVisitor {
    public static final WKTWriter WKT_WRITER = new WKTWriter();
    private final StringBuilder builder;
    private final IFieldMapper fieldMapper;

    public Expression2CswVisitor(StringBuilder builder, IFieldMapper fieldMapper) {
        this.builder = builder;
        this.fieldMapper = fieldMapper;
    }

    private static String quoteString(String text) {
        return text.replace("\"", "\\\"");
    }

    @Override
    public Object visit(PropertyName expr, Object extraData) {
        builder.append(fieldMapper.map(expr.getPropertyName()));
        return expr;
    }

    @Override
    public Object visit(Literal expr, Object extraData) {
        if (expr.getValue() instanceof Geometry) {
            Geometry geometry = (Geometry) expr.getValue();
            geometry2wkt(geometry);
        } else {
            builder.append("\"").append(quoteString(expr.getValue().toString())).append("\"");
        }
        return expr;
    }

    private void geometry2wkt(Geometry geometry) {
        try {
            final CoordinateReferenceSystem sourceCRS;
            if (geometry.getUserData() != null && geometry.getUserData() instanceof CoordinateReferenceSystem) {
                sourceCRS = (CoordinateReferenceSystem) geometry.getUserData();
            } else {
                sourceCRS = CRS.decode("EPSG:" + geometry.getSRID());
            }
            final CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");

            final MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
            geometry = JTS.transform(geometry, transform);
        } catch (FactoryException | TransformException e) {
            e.printStackTrace();
        }
        builder.append(WKT_WRITER.write(geometry));
    }


}
