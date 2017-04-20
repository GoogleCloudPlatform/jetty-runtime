# Instantiate session modules user selected
cd $JETTY_BASE
case "${SESSIONS:=local}" in
  memcached-datastore)
           set - $@ --module=session-cache-null,gcp-datastore,session-store-gcloud,gcp-xmemcached,session-store-cache
           ;;
  datastore)
           set - $@ --module=session-cache-null,gcp-datastore,session-store-gcloud
           ;;
  *)
           ;;
esac
