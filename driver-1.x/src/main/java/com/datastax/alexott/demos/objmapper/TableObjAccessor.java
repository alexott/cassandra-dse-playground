package com.datastax.alexott.demos.objmapper;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface TableObjAccessor {
    @Query("SELECT * from test.scala_test_complex where p1 = :p1 and p2 = :p2")
    Result<TableObjectClustered> getByPartKey(@Param int p1, @Param int p2);

    @Query("DELETE from test.scala_test_complex where p1 = :p1 and p2 = :p2")
    void deleteByPartKey(@Param int p1, @Param int p2);
}
