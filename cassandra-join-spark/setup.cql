create keyspace if not exists test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

use test;

create table if not exists stock_info (
  symbol text primary key,
  exchange text,
  industry text,
  name text,
  base_price double
);

truncate stock_info;
COPY stock_info (name, symbol, base_price, exchange, industry) FROM './src/main/resources/json/csv/exchangedata.csv';

