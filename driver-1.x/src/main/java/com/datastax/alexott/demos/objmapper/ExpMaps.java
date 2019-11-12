package com.datastax.alexott.demos;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

import java.text.DateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;

public class ExpMaps {

    public static void main(String[] args) throws InterruptedException {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        Cluster.Builder builder = Cluster.builder().addContactPoint(server);

        Locale locales[] = DateFormat.getAvailableLocales();
        System.out.println("We have " + locales.length + " locales");

        try (Cluster cluster = builder.build();
             Session session = cluster.connect()) {

            PreparedStatement stmt1 = session.prepare (
                    "update srs.entities set pop_a_ = pop_a_ + ? where hcom_geo_id = ?;");

            PreparedStatement stmt4 = session.prepare (
                    "select * from srs.entities_udt where hcom_geo_id = ?;");
            PreparedStatement stmt5 = session.prepare (
                    "select * from srs.entities where hcom_geo_id = ?;");

            MappingManager manager = new MappingManager(session);
            Mapper<ExpEntity> mapper = manager.mapper(ExpEntity.class);

            Random rnd = new Random();

            int maxCount = 10000;
            for (int i = 0; i < maxCount; i++) {
                int id = rnd.nextInt(maxCount);

                ResultSet rs;
                // insert via update
                for (int j = 0; j < locales.length; j++) {
                    rs = session.execute(stmt1.bind(
                            Collections.singletonMap(locales[j].getDisplayName(), rnd.nextDouble()), id));
                }
                rs = session.execute(stmt5.bind(id));
                if (rs != null) {
                    if (rs.one() != null) {
                    }
                }

                //
                ExpEntity entity = mapper.get(id);
                if (entity == null) {
                    entity = new ExpEntity();
                    entity.setHcom_geo_id(id);
                    entity.setPopularity(new HashSet<ExpPopularity>());
                }
                for (int j = 0; j < locales.length; j++) {
                    entity.getPopularity().add(new ExpPopularity(locales[j].getDisplayName(),
                            rnd.nextDouble(), rnd.nextDouble()));
                }
                mapper.save(entity);

                rs = session.execute(stmt4.bind(id));
                if (rs != null) {
                    if (rs.one() != null) {
                    }
                }
            }
        }
    }

}
