package org.elasticsearch.index.query;

import com.spatial4j.core.shape.Shape;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.GeoJSONShapeParser;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.geo.GeoShapeFieldMapper;
import org.elasticsearch.index.search.shape.ShapeFetchService;

import java.io.IOException;

public class GeoShapeQueryParser implements QueryParser {

    public static final String NAME = "geo_shape";

    private ShapeFetchService fetchService;

    public static class DEFAULTS {
        public static final String INDEX_NAME = "shapes";
        public static final String SHAPE_FIELD_NAME = "shape";
    }

    @Override
    public String[] names() {
        return new String[]{NAME, Strings.toCamelCase(NAME)};
    }

    @Override
    public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        String fieldName = null;
        ShapeRelation shapeRelation = null;
        Shape shape = null;

        String id = null;
        String type = null;
        String index = DEFAULTS.INDEX_NAME;
        String shapeFieldName = DEFAULTS.SHAPE_FIELD_NAME;

        XContentParser.Token token;
        String currentFieldName = null;
        float boost = 1f;

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                fieldName = currentFieldName;

                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();

                        token = parser.nextToken();
                        if ("shape".equals(currentFieldName)) {
                            shape = GeoJSONShapeParser.parse(parser);
                        } else if ("relation".equals(currentFieldName)) {
                            shapeRelation = ShapeRelation.getRelationByName(parser.text());
                            if (shapeRelation == null) {
                                throw new QueryParsingException(parseContext.index(), "Unknown shape operation [" + parser.text() + " ]");
                            }
                        } else if ("indexed_shape".equals(currentFieldName)) {
                            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                                if (token == XContentParser.Token.FIELD_NAME) {
                                    currentFieldName = parser.currentName();
                                } else if (token.isValue()) {
                                    if ("id".equals(currentFieldName)) {
                                        id = parser.text();
                                    } else if ("type".equals(currentFieldName)) {
                                        type = parser.text();
                                    } else if ("index".equals(currentFieldName)) {
                                        index = parser.text();
                                    } else if ("shape_field_name".equals(currentFieldName)) {
                                        shapeFieldName = parser.text();
                                    }
                                }
                            }
                            if (id == null) {
                                throw new QueryParsingException(parseContext.index(), "ID for indexed shape not provided");
                            } else if (type == null) {
                                throw new QueryParsingException(parseContext.index(), "Type for indexed shape not provided");
                            }
                            shape = fetchService.fetch(id, type, index, shapeFieldName);
                        }
                    }
                }
            } else if (token.isValue()) {
                if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                }
            }
        }

        if (shape == null) {
            throw new QueryParsingException(parseContext.index(), "No Shape defined");
        } else if (shapeRelation == null) {
            throw new QueryParsingException(parseContext.index(), "No Shape Relation defined");
        }

        MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
        if (smartNameFieldMappers == null || !smartNameFieldMappers.hasMapper()) {
            throw new QueryParsingException(parseContext.index(), "Failed to find geo_shape field [" + fieldName + "]");
        }

        FieldMapper fieldMapper = smartNameFieldMappers.mapper();
        // TODO: This isn't the nicest way to check this
        if (!(fieldMapper instanceof GeoShapeFieldMapper)) {
            throw new QueryParsingException(parseContext.index(), "Field [" + fieldName + "] is not a geo_shape");
        }

        GeoShapeFieldMapper shapeFieldMapper = (GeoShapeFieldMapper) fieldMapper;

        Query query = shapeFieldMapper.spatialStrategy().createQuery(shape, shapeRelation);
        query.setBoost(boost);
        return query;
    }

    @Inject(optional = true)
    public void setFetchService(@Nullable ShapeFetchService fetchService) {
        this.fetchService = fetchService;
    }
}