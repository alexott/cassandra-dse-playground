package com.datastax.alexott.demos

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.MappingManager
import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table

@Table(keyspace = "test", name = "app_category_agg")
class AppCategoryAggData {

    @PartitionKey
    lateinit var category: String

    @Column(name = "app_count")
    var appCount: Int = 0

    @Column(name = "sp_count")
    var spCount: Int = 0

    @Column(name = "subscriber_count")
    var subscriberCount: Int = 0

    @Column(name = "window_revenue")
    var windowRevenue: Long = 0

    @Column(name = "top_apps")
    var topApps: List<Map<String, Int>> = emptyList()

    override fun toString(): String {
        return "AppCategoryAggData(category='$category', appCount=$appCount, spCount=$spCount, subscriberCount=$subscriberCount, windowRevenue=$windowRevenue, topApps=$topApps)"
    }
}

object KtTestObjMapper {
    @JvmStatic
    fun main(args: Array<String>) {
        val cluster = Cluster.builder()
                .addContactPoint("10.101.34.176")
                .build()
        val session = cluster.connect()

        val manager = MappingManager(session)
        val mapper = manager.mapper(AppCategoryAggData::class.java)

        val appObj = AppCategoryAggData()
        appObj.category = "kotlin"
        appObj.appCount = 5
        appObj.spCount = 10
        appObj.subscriberCount = 50
        appObj.windowRevenue = 10000
        appObj.topApps = listOf(mapOf("t2" to 2))
        mapper.save(appObj)

        val obj2 = mapper.get("test")
        println("Object from =$obj2")

        session.close()
        cluster.close()

    }

}