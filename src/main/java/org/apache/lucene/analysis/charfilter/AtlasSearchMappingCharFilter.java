package org.apache.lucene.analysis.charfilter;

import java.io.Reader;

public class AtlasSearchMappingCharFilter extends MappingCharFilter {

	public AtlasSearchMappingCharFilter(NormalizeCharMap normMap, Reader input) {
		super(normMap, input);
	}

}
