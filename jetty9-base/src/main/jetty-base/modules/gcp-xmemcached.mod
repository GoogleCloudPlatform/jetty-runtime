[description]
Memcache cache for SessionData

[tags]
session

[depends]
session-store
slf4j-api

[lib]
lib/jetty-memcached-sessions-${jetty.version}.jar

[xml]
etc/sessions/session-data-cache/xmemcached.xml

[provides]
sessions/session-data-cache/xmemcached
