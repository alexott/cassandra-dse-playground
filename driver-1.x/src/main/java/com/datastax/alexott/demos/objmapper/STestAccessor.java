package com.datastax.alexott.demos;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface STestAccessor {
	@Query("SELECT * FROM test.stest WHERE solr_query = :solr")

	Result<STest> getViaSolr(@Param("solr") String solr);
}
