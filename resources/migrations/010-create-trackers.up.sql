create table trackers (
  id bigint auto_increment primary key,
  tracker_code varchar(256) not null,
  latest_activity timestamp,
  -- TODO owner_id bigint not null references users on delete cascade,
  owner_id bigint not null,
  public boolean not null default false,
  shared_secret varchar(64),
  password varchar(64),
  name varchar(256),
  description varchar(256),
  updated_at timestamp not null default now(),
  created_at timestamp not null default now()
);

create index ix_trackers_name on trackers(name);

create unique index uix_tracker_code on trackers(tracker_code);
