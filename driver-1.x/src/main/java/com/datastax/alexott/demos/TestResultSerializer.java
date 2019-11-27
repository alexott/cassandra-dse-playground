package com.datastax.alexott.demos;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/*

drop table if exists test.rstest;
drop type if exists test.tudt;
create type test.tudt (
  id int,
  t text,
  lst frozen<list<int>>
);

create table test.rstest (
  id int primary key,
  text text,
  date date,
  timestamp timestamp,
  time time,
  uuid uuid,
  tuuid timeuuid,
  m1 map<int, text>,
  m2 frozen<map<text,int>>,
  l1 list<int>,
  l2 frozen<list<int>>,
  s1 set<int>,
  s2 frozen<set<text>>,
  udt test.tudt,
  ludt1 list<frozen<test.tudt>>,
  ludt2 frozen<list<test.tudt>>,
  blob blob,
  bool boolean,
  dec decimal,
  double double,
  float float,
  bigint bigint,
  smallint smallint,
  tinyint tinyint,
  varint varint,
  ascii ascii,
  tuple tuple<int, text, float>,
  varchar varchar,
  nullval text
);

insert into test.rstest(id, text, date, timestamp, time, uuid, tuuid, m1, m2, l1, l2, s1, s2,
  udt, ludt1, ludt2, blob, bool, dec, double, float, bigint, smallint, tinyint, varint, ascii, tuple, varchar)
  values (1, 'text', '2019-01-29', toTimestamp(now()), '04:05:00.234', 123e4567-e89b-12d3-a456-426655440000,
  now(), {1:'m1', 2:'m2'}, {'m1':1, 'm2':2}, [1,2,3], [1,2,3], {1,2,3}, {'1','2','3'},
  {id: 1, t: 'text', lst: [1,2,3]}, [{id: 1, t: 'text', lst: [1,2,3]}, {id: 2, t: 'text2'}],
  [{id: 1, t: 'text', lst: [1,2,3]}, {id: 2, t: 'text2'}], bigintAsBlob(1024), true, 123562352352.0,
  10.015, 20.030, 123562352352, 10000, 10, 124325345634643900999999, 'ascii', (1, 'text', 10.0), 'varchar');

 */

public class TestResultSerializer {
	public static void main(String[] args) throws JsonProcessingException {
        String server = System.getProperty("contactPoint", "127.0.0.1");
		try (Cluster cluster = Cluster.builder().addContactPoint(server).build();
		Session session = cluster.connect()) {

			ResultSet rs = session.execute("select json * from test.rstest ;");
			int i = 0;
			System.out.print("[");
			for (Row row : rs) {
				if (i > 0)
					System.out.print(",");
				i++;
				String json = row.getString(0);
				System.out.print(json);
			}
			System.out.println("]");

			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = new SimpleModule();
			module.addSerializer(ResultSet.class, new ResultSetSerializer());
			mapper.registerModule(module);

			rs = session.execute("select * from test.rstest ;");
			String json = mapper.writeValueAsString(rs);
			System.out.println(json);
		}
	}

}
