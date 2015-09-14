create table events (
  id bigint auto_increment primary key,
  tracker_id bigint not null references trackers on delete cascade,
  event_session_id bigint not null references event_sessions on delete cascade,
  event_time timestamp not null,
  latitude decimal,
  longitude decimal,
  horizontal_accuracy decimal(10,2),
  vertical_accuracy decimal(10,2),
  speed decimal(5,2),
  heading decimal(5,2),
  satellite_count integer,
  altitude decimal,
  updated_at timestamp not null default now(),
  created_at timestamp not null default now()
  );

create index ix_events_event_time on events (tracker_id, event_time);
create index ix_events_created_at on events (tracker_id, created_at);
create index ix_events_trackers_id on events (tracker_id, event_session_id);
